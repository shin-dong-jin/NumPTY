package com.numpty.framework.security.web.filter;

import com.numpty.framework.exception.AccessDeniedException;
import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.security.web.matcher.AuthorizationRule;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.*;

import java.io.IOException;
import java.util.List;

public class AuthorizationFilter implements Filter {

    private final List<AuthorizationRule> rules;

    public AuthorizationFilter(List<AuthorizationRule> rules) {
        this.rules = rules;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServerRequest serverRequest = ServerRequest.from(request);
        ServerResponse serverResponse = ServerResponse.from(response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AuthorizationRule applicable = findApplicableRule(serverRequest);

        if (applicable == null) {
            throw new AccessDeniedException("No matches rule for " + serverRequest.getRequestURI());
        }

        if (!applicable.isAllowed(authentication)) {
            throw new AccessDeniedException("Access denied for " + serverRequest.getRequestURI());
        }

        chain.doFilter(request, response);
    }

    private AuthorizationRule findApplicableRule(ServerRequest serverRequest) {
        for (AuthorizationRule rule : rules) {
            if (rule.matches(serverRequest)) {
                return rule;
            }
        }

        return null;
    }
}
