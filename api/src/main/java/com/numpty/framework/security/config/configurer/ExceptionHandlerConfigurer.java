package com.numpty.framework.security.config.configurer;

import com.numpty.framework.security.config.SecurityConfigurer;
import com.numpty.framework.security.web.exception.AccessDeniedHandler;
import com.numpty.framework.security.web.exception.AuthenticationEntryPoint;
import com.numpty.framework.security.web.exception.DefaultAccessDeniedHandler;
import com.numpty.framework.security.web.exception.DefaultAuthenticationEntryPoint;
import com.numpty.framework.security.web.filter.ExceptionTranslationFilter;
import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.security.config.HttpSecurity;

public class ExceptionHandlerConfigurer implements SecurityConfigurer {

    private AuthenticationEntryPoint authenticationEntryPoint;
    private AccessDeniedHandler accessDeniedHandler;

    public ExceptionHandlerConfigurer authenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        return this;
    }

    public ExceptionHandlerConfigurer accessDeniedHandler(AccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        JsonMapper jsonMapper = http.getSharedObject(JsonMapper.class);

        AuthenticationEntryPoint entryPoint = this.authenticationEntryPoint != null
                ? this.authenticationEntryPoint
                : new DefaultAuthenticationEntryPoint(jsonMapper);

        AccessDeniedHandler deniedHandler = this.accessDeniedHandler != null
                ? this.accessDeniedHandler
                : new DefaultAccessDeniedHandler();

        http.doAddFilter(new ExceptionTranslationFilter(entryPoint, deniedHandler));
    }
}
