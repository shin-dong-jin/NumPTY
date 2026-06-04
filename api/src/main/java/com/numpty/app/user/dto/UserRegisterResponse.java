package com.numpty.app.user.dto;

import java.time.Instant;

public record UserRegisterResponse(String email, String name, Instant createdAt) {

}
