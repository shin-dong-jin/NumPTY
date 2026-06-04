package com.numpty.framework.security.config.configurer;

import com.numpty.framework.security.config.SecurityConfigurer;
import com.numpty.framework.security.web.cors.CorsConfig;
import com.numpty.framework.security.web.filter.CorsFilter;
import com.numpty.framework.security.config.HttpSecurity;
import java.util.ArrayList;
import java.util.List;

public class CorsConfigurer implements SecurityConfigurer {

    private boolean disabled = false;
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    private List<String> allowedHeaders = new  ArrayList<>(List.of("*"));
    private List<String> exposedHeaders = new  ArrayList<>();
    private boolean allowCredentials = false;
    private long maxAge = 3600L;

    public CorsConfigurer disable() {
        this.disabled = true;
        return this;
    }

    public CorsConfigurer allowedOrigins(String... origins) {
        this.allowedOrigins = new ArrayList<>(List.of(origins));
        return this;
    }

    public CorsConfigurer allowedMethods(String... methods) {
        this.allowedMethods = new ArrayList<>(List.of(methods));
        return this;
    }

    public CorsConfigurer allowedHeaders(String... headers) {
        this.allowedHeaders = new ArrayList<>(List.of(headers));
        return this;
    }

    public  CorsConfigurer exposedHeaders(String... headers) {
        this.exposedHeaders = new ArrayList<>(List.of(headers));
        return this;
    }

    public CorsConfigurer allowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    public CorsConfigurer maxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        if (disabled) {
            return;
        }

        if (allowCredentials && allowedOrigins.contains("*")) {
            throw new IllegalStateException("You cannot set both allowCredentials and allowedOrigins. Specify exact origins instead.");
        }

        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException("CORS is enabled but no allowedOrigins specified. Call allowedOrigins() or disable CORS.");
        }

        CorsConfig config = new CorsConfig(allowedOrigins, allowedMethods, allowedHeaders, exposedHeaders, allowCredentials, maxAge);

        http.doAddFilter(new CorsFilter(config));
    }
}
