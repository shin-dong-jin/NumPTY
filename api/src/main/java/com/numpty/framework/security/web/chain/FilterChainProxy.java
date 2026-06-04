package com.numpty.framework.security.web.chain;

import com.numpty.framework.context.ApplicationContext;
import com.numpty.framework.exception.ExceptionResolvers;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

public class FilterChainProxy implements Filter {

    private final List<SecurityFilterChain> filterChains;

    public FilterChainProxy(List<SecurityFilterChain> filterChains) {
        this.filterChains = filterChains;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServerRequest serverRequest = ServerRequest.from(request);
        ServerResponse serverResponse = ServerResponse.from(response);

        try {
            List<Filter> filters = getFilters(serverRequest);

            if (filters == null || filters.isEmpty()) {
                chain.doFilter(serverRequest, serverResponse);
                return;
            }

            VirtualFilterChain virtualFilterChain = new VirtualFilterChain(chain, filters);
            virtualFilterChain.doFilter(serverRequest, serverResponse);
        } catch (Exception e) {
            ServletContext servletContext = request.getServletContext();
            ApplicationContext applicationContext = (ApplicationContext) servletContext.getAttribute("APPLICATION_CONTEXT");
            applicationContext.getBean(ExceptionResolvers.class).findAndResolve(e, serverRequest, serverResponse);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private List<Filter> getFilters(HttpServletRequest httpRequest) {
        for (SecurityFilterChain chain : this.filterChains) {
            if (chain.matches(httpRequest)) {
                return chain.getFilters();
            }
        }

        return null;
    }

    private static final class VirtualFilterChain implements FilterChain {

        private final FilterChain originalChain;
        private final List<Filter> additionalFilters;
        private int currentPosition = 0;

        public VirtualFilterChain(FilterChain originalChain, List<Filter> additionalFilters) {
            this.originalChain = originalChain;
            this.additionalFilters = List.copyOf(additionalFilters);
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response)
                throws IOException, ServletException {
            if (currentPosition == additionalFilters.size()) {
                this.originalChain.doFilter(request, response);
                return;
            }

            this.currentPosition++;
            Filter nextFilter = this.additionalFilters.get(this.currentPosition - 1);

            nextFilter.doFilter(request, response, this);
        }
    }
}
