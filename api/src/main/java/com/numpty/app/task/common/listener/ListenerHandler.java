package com.numpty.app.task.common.listener;

import java.util.Map;

public interface ListenerHandler {
    boolean supports(Map<String, String> data);

    void handle(Map<String, String> data);
}
