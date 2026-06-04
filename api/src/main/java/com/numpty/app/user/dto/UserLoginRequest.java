package com.numpty.app.user.dto;

import java.util.regex.Pattern;

public record UserLoginRequest(String email, String password) {

    public UserLoginRequest {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email required.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password required.");
        }
    }
}
