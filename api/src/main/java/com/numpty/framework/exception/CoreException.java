package com.numpty.framework.exception;

import com.numpty.framework.web.http.HttpStatus;

public class CoreException extends ProjectException {

    private final HttpStatus status;

    public CoreException(String message) {
        super(message);
        status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public CoreException(String message, Throwable cause) {
        super(message, cause);
        status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public CoreException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CoreException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
