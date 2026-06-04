package com.numpty.framework.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ExceptionResolver {

    boolean supports(Exception exception);

    void resolve(Exception exception, HttpServletRequest request, HttpServletResponse response);
}
