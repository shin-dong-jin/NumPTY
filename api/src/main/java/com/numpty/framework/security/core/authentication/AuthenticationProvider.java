package com.numpty.framework.security.core.authentication;

public interface AuthenticationProvider {

    Authentication authenticate(Authentication authentication) throws RuntimeException;

    boolean supports(Class<?> authentication);
}
