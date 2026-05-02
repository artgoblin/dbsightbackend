package com.dbsight.neo.modal;

import java.util.Set;

public record SignupRequest(String username, String password, String email, Set<String> roles) {
    
}
