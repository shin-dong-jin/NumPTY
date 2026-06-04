package com.numpty.framework.exception;

public class InsufficientAuthenticationException extends ProjectException {
    public InsufficientAuthenticationException(String message) {
        super(message);
    }

    public InsufficientAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
