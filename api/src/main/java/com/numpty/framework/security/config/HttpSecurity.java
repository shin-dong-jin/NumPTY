package com.numpty.framework.security.config;

import com.numpty.framework.security.config.configurer.*;
import com.numpty.framework.security.web.chain.DefaultSecurityFilterChain;
import com.numpty.framework.security.web.chain.SecurityChainOrder;
import com.numpty.framework.security.web.chain.SecurityFilterChain;
import com.numpty.framework.security.web.matcher.RequestMatcher;
import com.numpty.framework.security.web.matcher.RequestMatcherImpl;
import jakarta.servlet.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class HttpSecurity {

    private RequestMatcher requestMatcher = request -> true;
    private int order;
    private final List<SecurityConfigurer> configurers = new ArrayList<>();
    private final List<Filter> filters = new ArrayList<>();
    private final Map<Class<?>, Object> sharedObjects = new HashMap<>();

    public HttpSecurity securityMatchers(String pattern) {
        this.requestMatcher = new RequestMatcherImpl(pattern);
        return this;
    }

    public HttpSecurity order(SecurityChainOrder securityChainOrder) {
        this.order = securityChainOrder.getValue();
        return this;
    }

    public <C extends SecurityConfigurer> C apply (C configurer) {
        configurers.add(configurer);
        return configurer;
    }

    @SuppressWarnings("unchecked")
    public <C extends SecurityConfigurer> C getOrApply(Class<C> type, Supplier<C> factory) {
        for (SecurityConfigurer configurer : configurers) {
            if (type.isInstance(configurer)) {
                return (C) configurer;
            }
        }

        return apply(factory.get());
    }

    public void doAddFilter(Filter filter) {
        this.filters.add(filter);
    }

    public HttpSecurity addFilter(Filter filter) {
        apply(new DefaultConfigurer(filter));
        return this;
    }

    public <T> HttpSecurity setSharedObject(Class<T> type, T object) {
        sharedObjects.put(type, object);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSharedObject(Class<T> type) {
        return (T) sharedObjects.get(type);
    }

    public HttpSecurity cors(Customizer<CorsConfigurer> customizer) {
        CorsConfigurer configurer = getOrApply(CorsConfigurer.class, CorsConfigurer::new);
        customizer.customize(configurer);
        return this;
    }

    public HttpSecurity csrf(Customizer<CsrfConfigurer> customizer) {
        CsrfConfigurer configurer = getOrApply(CsrfConfigurer.class, CsrfConfigurer::new);
        customizer.customize(configurer);
        return this;
    }

    public HttpSecurity exceptionHandling(Customizer<ExceptionHandlerConfigurer> customizer) {
        ExceptionHandlerConfigurer configurer = getOrApply(ExceptionHandlerConfigurer.class, ExceptionHandlerConfigurer::new);
        customizer.customize(configurer);
        return this;
    }

    public HttpSecurity authorizeHttpRequests(Customizer<AuthorizeRequestsConfigurer> customizer) {
        AuthorizeRequestsConfigurer configurer = getOrApply(AuthorizeRequestsConfigurer.class, AuthorizeRequestsConfigurer::new);
        customizer.customize(configurer);
        return this;
    }

    public SecurityFilterChain build() {
        for (SecurityConfigurer configurer : configurers) {
            configurer.init(this);
        }

        for (SecurityConfigurer configurer : configurers) {
            configurer.configure(this);
        }

        return new DefaultSecurityFilterChain(requestMatcher, List.copyOf(filters), order);
    }
}
