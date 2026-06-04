package com.numpty.framework.security.web.filter;

import com.numpty.framework.security.web.cors.CorsConfig;
import com.numpty.framework.web.http.HttpMethod;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter implements Filter {

    private final CorsConfig config;

    public CorsFilter(CorsConfig config) {
        this.config = config;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = ServerRequest.from(request);
        HttpServletResponse resp = ServerResponse.from(response);

        String origin = req.getHeader("Origin");

        if (origin == null) {
            chain.doFilter(req, resp);
            return;
        }

        if (!config.isOriginAllowed(origin)) {
            chain.doFilter(req, resp);
            return;
        }

        if (isPreflight(req)) {
            handlePreflight(resp);
            return;
        }

        chain.doFilter(req, resp);

        addCorsHeaders(resp, origin);

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
        resp.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    private void addCorsHeaders(HttpServletResponse resp, String origin) {
        resp.setHeader("Access-Control-Allow-Origin", origin);
        resp.setHeader("Vary", "Origin");

        if (config.isAllowCredentials()) {
            resp.setHeader("Access-Control-Allow-Credentials", "true");
        }

        if (!config.getExposedHeaders().isEmpty()) {
            resp.setHeader("Access-Control-Expose-Headers", String.join(",", config.getExposedHeaders()));
        }
    }

    private boolean isPreflight(HttpServletRequest req) {
        return HttpMethod.OPTIONS == HttpMethod.fromString(req.getMethod())
            && req.getHeader("Access-Control-Request-Method") != null;
    }

    private void handlePreflight(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Methods", String.join(",", config.getAllowedMethods()));
        resp.setHeader("Access-Control-Allow-Headers", String.join(",", config.getAllowedHeaders()));
        resp.setHeader("Access-Control-Max-Age", String.valueOf(config.getMaxAge()));
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
