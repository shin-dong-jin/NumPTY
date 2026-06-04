package com.numpty.framework.security.web.matcher;

import com.numpty.framework.security.core.authentication.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

public record AuthorizationRule(RequestMatcher matcher, Predicate<Authentication> check) {

    public boolean matches(HttpServletRequest request) {
        return matcher.matches(request);
    }

    public boolean isAllowed(Authentication authentication) {
        return check.test(authentication);
    }

    public static AuthorizationRule permitAll(String pattern) {
        return new AuthorizationRule(new RequestMatcherImpl(pattern), auth -> true);
    }

    public static AuthorizationRule denyAll(String pattern) {
        return new AuthorizationRule(new RequestMatcherImpl(pattern), auth -> false);
    }

    public static AuthorizationRule authenticated(String pattern) {
        return new AuthorizationRule(
            new RequestMatcherImpl(pattern),
            auth -> auth != null && auth.isAuthenticated()
        );
    }
}
