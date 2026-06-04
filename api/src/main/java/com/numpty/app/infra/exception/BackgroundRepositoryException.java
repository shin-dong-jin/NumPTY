package com.numpty.app.infra.exception;

import com.numpty.framework.exception.api.BackgroundException;

public class BackgroundRepositoryException extends BackgroundException {

    public BackgroundRepositoryException() {
        super();
    }

    public BackgroundRepositoryException(String message) {
        super(message);
    }

    public BackgroundRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
