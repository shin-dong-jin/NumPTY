package com.numpty.framework.security.config.configurer;

import com.numpty.framework.security.config.SecurityConfigurer;
import com.numpty.framework.security.web.csrf.CsrfTokenRepository;
import com.numpty.framework.security.web.csrf.HttpSessionCsrfTokenRepository;
import com.numpty.framework.security.web.filter.CsrfFilter;
import com.numpty.framework.security.config.HttpSecurity;

public class CsrfConfigurer implements SecurityConfigurer {

    private boolean disabled = false;
    private CsrfTokenRepository tokenRepository = new HttpSessionCsrfTokenRepository();

    public CsrfConfigurer disable() {
        this.disabled = true;
        return this;
    }

    public CsrfConfigurer tokenRepository(CsrfTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        if (disabled) {
            return;
        }

        http.doAddFilter(new CsrfFilter(tokenRepository));
    }
}
