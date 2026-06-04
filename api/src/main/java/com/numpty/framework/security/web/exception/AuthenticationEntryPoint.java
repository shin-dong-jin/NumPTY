package com.numpty.framework.security.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthenticationEntryPoint {

    public void commence(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException;
}
