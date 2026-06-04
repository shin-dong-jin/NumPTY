package com.numpty.framework.security.web.filter;

import com.numpty.framework.exception.AccessDeniedException;
import com.numpty.framework.exception.AuthenticationException;
import com.numpty.framework.exception.InsufficientAuthenticationException;
import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.security.web.exception.AccessDeniedHandler;
import com.numpty.framework.security.web.exception.AuthenticationEntryPoint;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ExceptionTranslationFilter implements Filter {

    private final AuthenticationEntryPoint entryPoint;
    private final AccessDeniedHandler accessDeniedHandler;

    public ExceptionTranslationFilter(AuthenticationEntryPoint entryPoint, AccessDeniedHandler accessDeniedHandler) {
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (AuthenticationException e) {
            handleAuthenticationException(ServerRequest.from(request), ServerResponse.from(response), e);
        } catch (AccessDeniedException e) {
            handleAccessDeniedException(ServerRequest.from(request), ServerResponse.from(response), e);
        }
    }

    private void handleAuthenticationException(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        SecurityContextHolder.clearContext();
        entryPoint.commence(request, response, exception);
    }

    private void handleAccessDeniedException(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication == null || !authentication.isAuthenticated();

        if (isAnonymous) {
            entryPoint.commence(request, response, new InsufficientAuthenticationException("Authentication required"));
            return;
        }

        accessDeniedHandler.handle(request, response, exception);
    }
}
