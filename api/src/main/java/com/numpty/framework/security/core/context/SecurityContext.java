package com.numpty.framework.security.core.context;

import com.numpty.framework.security.core.authentication.Authentication;

public interface SecurityContext {

    Authentication getAuthentication();

    void setAuthentication(Authentication authentication);
}
