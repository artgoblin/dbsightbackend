package com.dbsight.neo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dbsight.neo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);
}