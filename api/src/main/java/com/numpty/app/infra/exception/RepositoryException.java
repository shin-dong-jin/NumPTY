package com.numpty.app.infra.exception;

import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.api.BusinessException;

public class RepositoryException extends BusinessException {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
