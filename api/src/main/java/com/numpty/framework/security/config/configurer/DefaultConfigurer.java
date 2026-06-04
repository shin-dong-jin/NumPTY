package com.numpty.framework.security.config.configurer;

import com.numpty.framework.security.config.HttpSecurity;
import com.numpty.framework.security.config.SecurityConfigurer;
import jakarta.servlet.Filter;


public class DefaultConfigurer implements SecurityConfigurer {

    private final Filter filter;

    public DefaultConfigurer(Filter filter) {
        this.filter = filter;
    }

    @Override
    public void configure(HttpSecurity http) {
        http.doAddFilter(filter);
    }
}
