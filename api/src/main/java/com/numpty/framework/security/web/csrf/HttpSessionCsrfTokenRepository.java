package com.numpty.framework.security.web.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.UUID;

public class HttpSessionCsrfTokenRepository implements CsrfTokenRepository {

    private static final String SESSION_ATTR = "CSRF_TOKEN";

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        String value = UUID.randomUUID().toString();
        return new CsrfToken("X-CSRF-TOKEN", "_csrf", value);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();

        if (token == null) {
            session.removeAttribute(SESSION_ATTR);
            return;
        }

        session.setAttribute(SESSION_ATTR, token);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (CsrfToken) session.getAttribute(SESSION_ATTR);
    }
}
