package com.numpty.framework.web.api;

import java.time.LocalDateTime;

public class ErrorResponseBody {

    private final String timestamp;
    private final int status;
    private final String message;
    private final String path;

    public ErrorResponseBody(int status, String message, String path) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
