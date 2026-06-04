package com.numpty.framework.web.http;

import com.numpty.framework.exception.CoreException;

public class RequestParameter {

    private final String name;
    private final String value;

    public RequestParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public boolean isPresent() {
        return value != null && !value.isBlank();
    }

    public String asString() {
        if (value == null) {
            throw new CoreException("Missing request parameter: " + name, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return value;
    }

    public Long asLong() {
        return Long.valueOf(asString());
    }

    public Integer asInt() {
        return Integer.valueOf(asString());
    }
}
