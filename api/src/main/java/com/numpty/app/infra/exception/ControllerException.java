package com.numpty.app.infra.exception;

import com.numpty.framework.exception.api.BusinessException;

public class ControllerException extends BusinessException {

    public ControllerException(String message) {
        super(message);
    }
}
