package com.numpty.framework.security.web.filter;

import com.numpty.framework.exception.AuthorizationException;
import com.numpty.framework.security.web.csrf.CsrfToken;
import com.numpty.framework.security.web.csrf.CsrfTokenRepository;
import com.numpty.framework.web.http.HttpMethod;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.*;

import java.io.IOException;

public class CsrfFilter implements Filter {

    private final CsrfTokenRepository tokenRepository;

    public CsrfFilter(CsrfTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServerRequest serverRequest = ServerRequest.from(request);
        ServerResponse serverResponse = ServerResponse.from(response);

        CsrfToken token = tokenRepository.loadToken(serverRequest);
        boolean missingToken = (token == null);

        if (missingToken) {
            token = tokenRepository.generateToken(serverRequest);
            tokenRepository.saveToken(token, serverRequest, serverResponse);
        }

        serverRequest.setAttribute(CsrfToken.class.getName(), token);
        serverRequest.setAttribute(token.getParameterName(), token);

        if (HttpMethod.fromString(serverRequest.getMethod()).isCsrfSafe()) {
            chain.doFilter(serverRequest, serverResponse);
            return;
        }

        String actualToken = serverRequest.getHeader(token.getHeaderName());
        if (actualToken == null) {
            actualToken = serverRequest.getParameter(token.getParameterName());
        }

        if (!token.getToken().equals(actualToken)) {
            throw new AuthorizationException(missingToken ? "Missing CSRF token" : "Invalid CSRF token");
        }

        chain.doFilter(serverRequest, serverResponse);
    }
}
