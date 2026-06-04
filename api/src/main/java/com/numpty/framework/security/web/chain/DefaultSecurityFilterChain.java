package com.numpty.framework.security.web.chain;

import com.numpty.framework.security.web.matcher.RequestMatcher;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class DefaultSecurityFilterChain implements SecurityFilterChain {

    private final RequestMatcher requestMatcher;
    private final List<Filter> filters;
    private final int order;


    public DefaultSecurityFilterChain(RequestMatcher requestMatcher, List<Filter> filters,
        int order) {
        this.requestMatcher = requestMatcher;
        this.filters = filters;
        this.order = order;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return this.requestMatcher.matches(request);
    }

    @Override
    public List<Filter> getFilters() {
        return this.filters;
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
