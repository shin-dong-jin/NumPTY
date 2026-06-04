package com.numpty.framework.web.router;

import com.numpty.framework.web.handler.HandlerFunction;
import com.numpty.framework.web.http.HttpMethod;
import com.numpty.framework.support.PathMatcher;
import com.numpty.framework.web.http.MutableRequest;

import com.numpty.framework.web.http.ServerRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public class Router implements RouterFunction {

    private final HttpMethod method;
    private final PathMatcher pathMatcher;
    private final HandlerFunction handler;

    public Router(HttpMethod method, String pathPattern, HandlerFunction handler) {
        this.method = method;
        this.pathMatcher = new PathMatcher(pathPattern);
        this.handler = handler;
    }

    private Map<String, String> match(HttpMethod method, String requestPath) {
        if (this.method != method) {
            return null;
        }

        return pathMatcher.matchPath(requestPath);
    }

    @Override
    public HandlerFunction route(HttpServletRequest request) {
        ServerRequest serverRequest = ServerRequest.from(request);

        HttpMethod requestMethod = HttpMethod.fromString(serverRequest.getMethod());
        String requestPath = serverRequest.path();

        Map<String, String> pathVariables = match(requestMethod, requestPath);

        if (pathVariables == null) {
            return null;
        }

        serverRequest.putAllPathVariables(pathVariables);

        return handler;
    }
}
