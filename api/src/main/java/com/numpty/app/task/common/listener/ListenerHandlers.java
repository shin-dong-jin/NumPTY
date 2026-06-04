package com.numpty.app.task.common.listener;

import com.numpty.app.infra.exception.ListenerException;

import java.util.List;
import java.util.Map;

public class ListenerHandlers {

    private final List<ListenerHandler> listenerHandlers;

    public ListenerHandlers(List<ListenerHandler> listenerHandlers) {
        this.listenerHandlers = listenerHandlers;
    }

    public ListenerHandler findListenerHandler(Map<String, String> data) {
        return listenerHandlers.stream()
                .filter(handler -> handler.supports(data))
                .findFirst()
                .orElseThrow(() -> new ListenerException("Unsupported listener handler."));
    }
}
