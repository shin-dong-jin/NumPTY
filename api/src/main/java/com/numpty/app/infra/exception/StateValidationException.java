package com.numpty.app.infra.exception;

import com.numpty.framework.exception.api.BusinessException;
import com.numpty.framework.web.http.HttpStatus;

public class StateValidationException extends BusinessException {

    public StateValidationException(String message) {
        super(message);
    }

    public StateValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateValidationException(String message, HttpStatus status) {
        super(message, status);
    }

    public StateValidationException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
