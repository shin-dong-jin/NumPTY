package com.numpty.framework.security.core.authentication;

public interface Authentication {

    Object getPrincipal();

    Object getCredentials();

    boolean isAuthenticated();

    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
