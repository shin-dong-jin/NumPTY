package com.numpty.framework.web.response;

import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;

import java.util.List;

public class ReturnValueHandlers {

    private final List<ReturnValueHandler> returnValueHandlers;

    public ReturnValueHandlers(List<ReturnValueHandler> returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    public ReturnValueHandler findReturnValueHandler(Class<?> returnType) {
        return returnValueHandlers.stream()
                .filter(handler -> handler.supports(returnType))
                .findFirst()
                .orElseThrow(() -> new CoreException("Unsupported return type: " + returnType.getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
