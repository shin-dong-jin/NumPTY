package com.numpty.app.infra.auth;

import com.numpty.framework.security.core.authentication.Authentication;

public class JwtAuthenticationToken implements Authentication {

    private final Object principal;
    private final Object credentials;
    private boolean authenticated;

    public JwtAuthenticationToken(Object credentials) {
        this.principal = null;
        this.credentials = credentials;
        this.authenticated = false;
    }

    public JwtAuthenticationToken(Object principal, Object credentials) {
        this.principal = principal;
        this.credentials = credentials;
        this.authenticated = true;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                "Cannot set authenticated context trusted. Use constructor instead.");
        }

        this.authenticated = false;
    }
}
