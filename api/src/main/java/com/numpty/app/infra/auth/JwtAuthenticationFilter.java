package com.numpty.app.infra.auth;

import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.authentication.AuthenticationManager;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.web.http.ServerRequest;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class JwtAuthenticationFilter implements Filter {

    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest serverRequest = ServerRequest.from(request);

        String header = serverRequest.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        Authentication unauthenticated = new JwtAuthenticationToken(token);
        Authentication authenticated = authenticationManager.authenticate(unauthenticated);

        SecurityContextHolder.getContext().setAuthentication(authenticated);

        chain.doFilter(request, response);
    }
}
