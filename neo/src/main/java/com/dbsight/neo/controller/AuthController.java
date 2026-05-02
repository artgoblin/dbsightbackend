package com.dbsight.neo.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dbsight.neo.entity.User;
import com.dbsight.neo.feature.CustomUserDetailsFeature;
import com.dbsight.neo.feature.EmailFeature;
import com.dbsight.neo.feature.JwtTokenGeneratorFeature;
import com.dbsight.neo.helper.CustomUserDetailsHelper;
import com.dbsight.neo.modal.ForgotPasswordRequest;
import com.dbsight.neo.modal.LoginRequest;
import com.dbsight.neo.modal.LoginResponse;
import com.dbsight.neo.modal.ResetPasswordRequest;
import com.dbsight.neo.modal.SignupRequest;
import com.dbsight.neo.repository.UserRepository;
import com.dbsight.neo.service.QweryService;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGeneratorFeature jwtService;
    private final CustomUserDetailsFeature userDetailsService;
    private final EmailFeature emailService;
    private final QweryService qweryService;

    @Value("${spring.datasource.username}")
    private String defaultDbUsername;

    @Value("${spring.datasource.password}")
    private String defaultDbPassword;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtTokenGeneratorFeature jwtService,
            CustomUserDetailsFeature userDetailsService, EmailFeature emailService,
            QweryService qweryService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
        this.qweryService = qweryService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("ROLE_USER"));
        User savedUser = userRepository.save(user);

        // Add default database connection
        try {
            DatabaseConnectionDetails defaultDb = new DatabaseConnectionDetails();
            defaultDb.setConnectionName("Default Adventureworks");
            defaultDb.setDbType("postgresql");
            defaultDb.setHost("postgres");
            defaultDb.setPort("5432");
            defaultDb.setDatabaseName("Adventureworks");
            defaultDb.setUsername(defaultDbUsername);
            defaultDb.setPassword(defaultDbPassword);
            
            qweryService.newDbConnection(defaultDb, savedUser.getId());
        } catch (Exception e) {
            // Log error but don't fail signup
            System.err.println("Failed to add default connection: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        CustomUserDetailsHelper userDetails = (CustomUserDetailsHelper) userDetailsService
                .loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);

        // Ensure default database connection exists for the user
        try {
            DatabaseConnectionDetails defaultDb = new DatabaseConnectionDetails();
            defaultDb.setConnectionName("Default Adventureworks");
            defaultDb.setDbType("postgresql");
            defaultDb.setHost("postgres");
            defaultDb.setPort("5432");
            defaultDb.setDatabaseName("Adventureworks");
            defaultDb.setUsername(defaultDbUsername);
            defaultDb.setPassword(defaultDbPassword);
            
            qweryService.newDbConnection(defaultDb, userDetails.getUserId());
        } catch (Exception e) {
            System.err.println("Failed to ensure default connection on login: " + e.getMessage());
        }

        return ResponseEntity.ok(new LoginResponse(token, userDetails.getUserId(), userDetails.getUsername()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Security: Always return the same message to prevent email enumeration
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });

        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
