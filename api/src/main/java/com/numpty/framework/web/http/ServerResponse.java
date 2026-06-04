package com.numpty.framework.web.http;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class ServerResponse extends HttpServletResponseWrapper {

    public ServerResponse(HttpServletResponse response) {
        super(response);
    }

    public static ServerResponse from(ServletResponse response) {
        if (response instanceof ServerResponse) {
            return (ServerResponse) response;
        }

        ServletResponse current = response;
        while (current instanceof HttpServletResponseWrapper wrapper) {
            ServletResponse wrapped = wrapper.getResponse();

            if (wrapped instanceof ServerResponse) {
                return (ServerResponse) wrapped;
            }

            current = wrapped;
        }

        if (response instanceof HttpServletResponse) {
            return new ServerResponse((HttpServletResponse) response);
        }

        throw new IllegalArgumentException("Invalid response type: " + response.getClass());
    }
}
