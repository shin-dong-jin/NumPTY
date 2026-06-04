package com.numpty.framework.exception;

public class AuthorizationException extends ProjectException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
