package com.numpty.app.infra.auth;

import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.security.web.exception.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public JwtAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        jsonMapper.write(response.getOutputStream(), Map.of(
                "error", "UNAUTHORIZED",
                "message", exception.getMessage()
        ));
    }
}
