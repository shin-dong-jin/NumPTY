package com.numpty.framework.exception.api;

import com.numpty.framework.exception.ProjectException;

public class BackgroundException extends ProjectException {

    public BackgroundException() {
        super();
    }

    public BackgroundException(String message) {
        super(message);
    }

    public BackgroundException(String message, Throwable cause) {
        super(message, cause);
    }
}
