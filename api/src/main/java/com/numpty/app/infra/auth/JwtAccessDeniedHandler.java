package com.numpty.app.infra.auth;

import com.numpty.framework.exception.AccessDeniedException;
import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.security.web.exception.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public JwtAccessDeniedHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        jsonMapper.write(response.getOutputStream(), Map.of(
                "error", "FORBIDDEN",
                "message", exception.getMessage()
        ));
    }
}
