package com.numpty.framework.web.router;

import com.numpty.framework.web.handler.HandlerFunction;
import com.numpty.framework.web.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

public class RouterRegistry {

    private final List<RouterFunction> routers = new ArrayList<>();

    public RouterRegistry GET(String pathPattern, HandlerFunction handler) {
        routers.add(new Router(HttpMethod.GET, pathPattern, handler));
        return this;
    }

    public RouterRegistry POST(String pathPattern, HandlerFunction handler) {
        routers.add(new Router(HttpMethod.POST, pathPattern, handler));
        return this;
    }

    public RouterRegistry PUT(String pathPattern, HandlerFunction handler) {
        routers.add(new Router(HttpMethod.PUT, pathPattern, handler));
        return this;
    }

    public RouterRegistry DELETE(String pathPattern, HandlerFunction handler) {
        routers.add(new Router(HttpMethod.DELETE, pathPattern, handler));
        return this;
    }

    public RouterFunction build() {
        return routers.stream().
                reduce(RouterFunction::and)
                .orElse(null);
    }
}
