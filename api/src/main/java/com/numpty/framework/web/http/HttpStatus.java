package com.numpty.framework.web.http;

public enum HttpStatus {

    OK(200), CREATED(201), ACCEPTED(202), NO_CONTENT(204), BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), NOT_FOUND(404), CONFLICT(409), INTERNAL_SERVER_ERROR(500), SERVICE_UNAVAILABLE(503);

    private final Integer code;

    HttpStatus(Integer code) {
        this.code = code;
    }

    public static HttpStatus fromString(String status) {
        return status == null ? null : HttpStatus.valueOf(status.toUpperCase());
    }

    public boolean isInfo() {
        return this == OK
                || this == CREATED
                || this == NO_CONTENT;
    }

    public boolean isWarn() {
        return this == BAD_REQUEST
                || this == UNAUTHORIZED
                || this == FORBIDDEN
                || this == NOT_FOUND;
    }

    public boolean isError() {
        return this == INTERNAL_SERVER_ERROR
                || this == SERVICE_UNAVAILABLE;
    }

    public Integer getCode() {
        return code;
    }
}
