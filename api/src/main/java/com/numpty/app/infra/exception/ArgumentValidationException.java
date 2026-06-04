package com.numpty.app.infra.exception;

import com.numpty.framework.exception.api.BusinessException;
import com.numpty.framework.web.http.HttpStatus;

public class ArgumentValidationException extends BusinessException {

    public ArgumentValidationException(String message) {
        super(message);
    }

    public ArgumentValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentValidationException(String message, HttpStatus status) {
        super(message, status);
    }

    public ArgumentValidationException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
