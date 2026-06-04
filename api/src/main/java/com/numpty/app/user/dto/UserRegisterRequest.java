package com.numpty.app.user.dto;

public record UserRegisterRequest(
        String email,
        String name,
        String password
) {

    public UserRegisterRequest {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email required.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name required.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password required.");
        }
    }
}
