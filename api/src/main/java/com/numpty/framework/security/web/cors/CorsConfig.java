package com.numpty.framework.security.web.cors;

import java.util.List;

public class CorsConfig {

    private final List<String> allowedOrigins;
    private final List<String> allowedMethods;
    private final List<String> allowedHeaders;
    private final List<String> exposedHeaders;
    private final boolean allowCredentials;
    private final long maxAge;

    public CorsConfig(List<String> allowedOrigins, List<String> allowedMethods,
        List<String> allowedHeaders, List<String> exposedHeaders, boolean allowCredentials,
        long maxAge) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.exposedHeaders = exposedHeaders;
        this.allowCredentials = allowCredentials;
        this.maxAge = maxAge;
    }

    public boolean isOriginAllowed(String origin) {
        if (origin == null) {
            return false;
        }

        return allowedOrigins.contains("*") || allowedOrigins.contains(origin);
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }
}
