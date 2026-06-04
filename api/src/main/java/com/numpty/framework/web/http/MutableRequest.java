package com.numpty.framework.web.http;

import com.numpty.framework.web.api.Request;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface MutableRequest extends Request {
    void putAllPathVariables(Map<String, String> pathVariables);

    HttpServletRequest getReq();
    HttpServletResponse getResp();
}
