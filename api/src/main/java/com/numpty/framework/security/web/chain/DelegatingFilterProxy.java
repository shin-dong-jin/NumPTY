package com.numpty.framework.security.web.chain;

import com.numpty.framework.context.ApplicationContext;
import jakarta.servlet.*;

import java.io.IOException;

public class DelegatingFilterProxy implements Filter {

    private final String targetBeanName;
    private final ApplicationContext appContext;
    private Filter delegate;

    public DelegatingFilterProxy(String targetBeanName, ApplicationContext appContext) {
        this.targetBeanName = targetBeanName;
        this.appContext = appContext;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.delegate == null) {
            this.delegate = appContext.getBean(this.targetBeanName, Filter.class);
        }

        if (this.delegate == null) {
            chain.doFilter(request, response);
            return;
        }

        this.delegate.doFilter(request, response, chain);
    }
}
