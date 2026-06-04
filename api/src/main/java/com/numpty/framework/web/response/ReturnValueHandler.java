package com.numpty.framework.web.response;

import com.numpty.framework.web.api.ModelAndView;
import com.numpty.framework.web.http.MutableRequest;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ReturnValueHandler {

    boolean supports(Class<?> returnType);

    ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception;
}
