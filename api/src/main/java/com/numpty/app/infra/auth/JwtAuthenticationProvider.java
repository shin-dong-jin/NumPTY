package com.numpty.app.infra.auth;

import com.numpty.framework.exception.AuthenticationException;
import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.authentication.AuthenticationProvider;
import com.numpty.framework.security.core.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationProvider(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws RuntimeException {
        String token = (String) authentication.getCredentials();

        try {
            Claims claims = jwtProvider.validateAndParseToken(token);
            String username = claims.getSubject();

            return new JwtAuthenticationToken(username, token);
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException("TOKEN_EXPIRED");
        } catch (JwtException e) {
            throw new AuthenticationException("INVALID_TOKEN");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
