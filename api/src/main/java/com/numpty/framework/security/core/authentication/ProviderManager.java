package com.numpty.framework.security.core.authentication;

import com.numpty.framework.exception.CoreException;

import java.util.ArrayList;
import java.util.List;

public class ProviderManager implements AuthenticationManager {

    private final List<AuthenticationProvider> providers = new ArrayList<>();

    @Override
    public void addProvider(AuthenticationProvider provider) {
        providers.add(provider);
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        for (AuthenticationProvider provider : providers) {
            if (provider.supports(authentication.getClass())) {
                return provider.authenticate(authentication);
            }
        }

        throw new CoreException("No authentication provider supports " + authentication.getClass().getName());
    }
}
