package com.numpty.framework.security.web.matcher;

import com.numpty.framework.web.http.ServerRequest;
import jakarta.servlet.http.HttpServletRequest;

public class RequestMatcherImpl implements RequestMatcher {

    private final String pattern;

    public RequestMatcherImpl(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        ServerRequest serverRequest = ServerRequest.from(request);
        String path = serverRequest.path();

        if (pattern.equals("/**")) {
            return true;
        }

        if (pattern.endsWith("/**")) {
            String base = pattern.substring(0, pattern.length() - 3);

            return path.equals(base) || path.startsWith(base + "/");
        }

        return path.equals(pattern);
    }
}
