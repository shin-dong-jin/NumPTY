package com.numpty.framework.exception.api;

import com.numpty.framework.exception.ProjectException;
import com.numpty.framework.web.http.HttpStatus;

public class BusinessException extends ProjectException {

    private final HttpStatus status;

    public BusinessException() {
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String message) {
        super(message);
        status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
