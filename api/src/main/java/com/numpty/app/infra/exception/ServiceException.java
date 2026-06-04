package com.numpty.app.infra.exception;

import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.api.BusinessException;

public class ServiceException extends BusinessException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, HttpStatus status) {
        super(message, status);
    }

    public ServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
