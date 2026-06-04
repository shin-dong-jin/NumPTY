package com.numpty.framework.security.core.authentication;

public interface AuthenticationManager {

    Authentication authenticate(Authentication authentication) throws RuntimeException;

    void addProvider(AuthenticationProvider provider);
}
