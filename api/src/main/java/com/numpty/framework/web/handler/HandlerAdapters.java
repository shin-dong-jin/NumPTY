package com.numpty.framework.web.handler;

import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;

import java.util.List;

public class HandlerAdapters {

    private final List<HandlerAdapter> handlerAdapters;

    public HandlerAdapters(List<HandlerAdapter> handlerAdapters) {
        this.handlerAdapters = handlerAdapters;
    }

    public HandlerAdapter findHandlerAdapter(Object handler) {
        return handlerAdapters.stream()
                .filter(adapter -> adapter.supports(handler))
                .findFirst()
                .orElseThrow(() -> new CoreException("Unsupported handler: " + handler.getClass().getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
