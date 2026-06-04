package com.numpty.framework.security.web.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface CsrfTokenRepository {

    CsrfToken generateToken(HttpServletRequest request);
    void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response);
    CsrfToken loadToken(HttpServletRequest request);
}
