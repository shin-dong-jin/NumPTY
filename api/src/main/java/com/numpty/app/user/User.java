package com.numpty.app.user;

import com.numpty.app.infra.exception.ArgumentValidationException;
import com.numpty.app.infra.exception.StateValidationException;

import java.time.Instant;
import java.util.UUID;

public class User {

    private UUID id;
    private String email;
    private String name;
    private String password;
    private UserStatus status;
    private UserRole role;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private User(UUID id, String email, String name, String password, UserStatus status,
        UserRole role, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.status = status;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static User createUser(String email, String name, String password) {
        if (email == null || email.isBlank()) {
            throw new ArgumentValidationException("Email required.");
        }

        if (name == null || name.isBlank()) {
            throw new ArgumentValidationException("Name required.");
        }

        if (password == null) {
            throw new ArgumentValidationException("Password required.");
        }

        if (!isValidEmail(email)) {
            throw new ArgumentValidationException("Invalid email format.");
        }

        if (name.length() > 50) {
            throw new ArgumentValidationException("name too long (max 50).");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Instant now = Instant.now();

        return User.builder()
            .id(UUID.randomUUID())
            .email(normalizedEmail)
            .name(name)
            .password(password)
            .status(UserStatus.ACTIVE)
            .role(UserRole.USER)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    public static User reconstituteUser(UUID id, String email, String name, String password, UserStatus status, UserRole role, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return User.builder()
            .id(id)
            .email(email)
            .name(name)
            .password(password)
            .status(status)
            .role(role)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .deletedAt(deletedAt)
            .build();
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ArgumentValidationException("name required");
        }

        if (name.length() > 50) {
            throw new ArgumentValidationException("name too long (max 50).");
        }

        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void changePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ArgumentValidationException("password required");
        }

        this.password = password;
        this.updatedAt = Instant.now();
    }

    public void withdraw() {
        if (this.status != UserStatus.ACTIVE) {
            throw new StateValidationException("Cannot withdraw inactive user");
        }

        Instant now = Instant.now();

        this.status = UserStatus.INACTIVE;
        this.deletedAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    private static class Builder {

        private UUID id;
        private String email;
        private String name;
        private String password;
        private UserStatus status;
        private UserRole role;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant deletedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder deletedAt(Instant deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public User build() {
            return new User(id, email, name, password, status, role, createdAt, updatedAt, deletedAt);
        }
    }

    private static Builder builder() {
        return new Builder();
    }
}
