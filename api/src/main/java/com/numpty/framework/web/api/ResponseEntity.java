package com.numpty.framework.web.api;

import com.numpty.framework.web.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntity<T> {

    private final HttpStatus status;
    private final Map<String, String> headers;
    private final T body;

    private ResponseEntity(HttpStatus status, Map<String, String> headers, T body) {
        this.status = status;
        this.headers = headers == null ? new HashMap<>() : headers;
        this.body = body;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    public static Builder status(HttpStatus status) {
        return new Builder(status);
    }

    public static Builder status(String status) {
        return new Builder(HttpStatus.fromString(status));
    }

    public static Builder ok() {
        return new Builder(HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(HttpStatus.OK, null, body);
    }

    public static Builder created() {
        return new Builder(HttpStatus.CREATED);
    }

    public static Builder accepted() {
        return new Builder(HttpStatus.ACCEPTED);
    }

    public static Builder noContent() {
        return new Builder(HttpStatus.NO_CONTENT);
    }

    public static Builder badRequest() {
        return new Builder(HttpStatus.BAD_REQUEST);
    }

    public static Builder unauthorized() {
        return new Builder(HttpStatus.UNAUTHORIZED);
    }

    public static Builder forbidden() {
        return new Builder(HttpStatus.FORBIDDEN);
    }

    public static Builder notFound() {
        return new Builder(HttpStatus.NOT_FOUND);
    }

    public static Builder internalError() {
        return new Builder(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class Builder {
        private final HttpStatus status;
        private final Map<String, String> headers = new HashMap<>();

        public Builder(HttpStatus status) {
            this.status = status;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public <T> ResponseEntity<T> body(T body) {
            return new ResponseEntity<>(status, headers, body);
        }

        public ResponseEntity<Void> build() {
            return new ResponseEntity<>(status, headers, null);
        }
    }
}
