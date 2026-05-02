package com.dbsight.neo.modal;

public record ResetPasswordRequest(String token, String newPassword) {
}