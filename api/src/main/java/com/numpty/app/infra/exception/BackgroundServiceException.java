package com.numpty.app.infra.exception;

import com.numpty.framework.exception.api.BackgroundException;

public class BackgroundServiceException extends BackgroundException {

    public BackgroundServiceException() {
        super();
    }

    public BackgroundServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackgroundServiceException(String message) {
        super(message);
    }
}
