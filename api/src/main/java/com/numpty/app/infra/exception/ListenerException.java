package com.numpty.app.infra.exception;

import com.numpty.framework.exception.api.BackgroundException;

public class ListenerException extends BackgroundException {

    public ListenerException(String message) {
        super(message);
    }

    public ListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
