package com.numpty.framework.security.web.exception;

import com.numpty.framework.support.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class DefaultAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public DefaultAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json?charset=UTF-8");

        jsonMapper.write(response.getOutputStream(), Map.of(
                "error", "UNAUTHROIZED",
                "message", exception != null ? exception.getMessage() : "Authentication required"
        ));
    }
}
