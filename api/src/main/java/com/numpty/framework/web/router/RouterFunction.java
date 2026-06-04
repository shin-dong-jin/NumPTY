package com.numpty.framework.web.router;

import com.numpty.framework.web.handler.HandlerFunction;
import com.numpty.framework.web.http.MutableRequest;
import com.numpty.framework.web.http.ServerRequest;
import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface RouterFunction {

    HandlerFunction route(HttpServletRequest request);

    default RouterFunction and(RouterFunction other) {
        return request -> {
            HandlerFunction handler = this.route(request);
            return handler == null ? other.route(request) : handler;
        };
    }
}
