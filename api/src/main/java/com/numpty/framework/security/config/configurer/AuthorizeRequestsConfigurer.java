package com.numpty.framework.security.config.configurer;

import com.numpty.framework.security.config.SecurityConfigurer;
import com.numpty.framework.security.web.filter.AuthorizationFilter;
import com.numpty.framework.security.web.matcher.AuthorizationRule;
import com.numpty.framework.security.config.HttpSecurity;

import java.util.ArrayList;
import java.util.List;

public class AuthorizeRequestsConfigurer implements SecurityConfigurer {

    private final List<AuthorizationRule> rules = new ArrayList<>();
    private String pattern;

    public AuthorizeRequestsConfigurer requestMatchers(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public AuthorizeRequestsConfigurer anyRequest() {
        this.pattern = "/**";
        return this;
    }

    public AuthorizeRequestsConfigurer permitAll() {
        if (pattern == null) {
            throw new IllegalStateException("Call requestMatchers() or anyRequest() first");
        }

        rules.add(AuthorizationRule.permitAll(pattern));
        pattern = null;
        return this;
    }

    public AuthorizeRequestsConfigurer denyAll() {
        if (pattern == null) {
            throw new IllegalStateException("Call requestMatchers() or anyRequest() first");
        }

        rules.add(AuthorizationRule.denyAll(pattern));
        pattern = null;
        return this;
    }

    public AuthorizeRequestsConfigurer authenticated() {
        if (pattern == null) {
            throw new IllegalStateException("Call requestMatchers() or anyRequest() first");
        }

        rules.add(AuthorizationRule.authenticated(pattern));
        pattern = null;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        http.doAddFilter(new AuthorizationFilter(List.copyOf(rules)));
    }
}
