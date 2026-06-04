package com.numpty.framework.web.http;

import java.util.Set;

public enum HttpMethod {

    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE;

    private static final Set<HttpMethod> REQUIRE_BODY_METHODS = Set.of(POST, PUT, DELETE, PATCH);
    private static final Set<HttpMethod> CSRF_SAFE_METHODS = Set.of(GET, HEAD, OPTIONS, TRACE);

    public static HttpMethod fromString(String method) {
        return method == null ? null : HttpMethod.valueOf(method.toUpperCase());
    }

    public boolean isBodyRequired() {
        return REQUIRE_BODY_METHODS.contains(this);
    }

    public boolean isCsrfSafe() {
        return CSRF_SAFE_METHODS.contains(this);
    }
}
