package com.numpty.framework.security.core.context;

import com.numpty.framework.exception.CoreException;

public class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private SecurityContextHolder() {
        throw new CoreException("Instantiation not allowed for this class.");
    }

    public static SecurityContext getContext() {
        SecurityContext context = CONTEXT_HOLDER.get();

        if (context == null) {
            context = new SecurityContextImpl();
            CONTEXT_HOLDER.set(context);
        }

        return context;
    }

    public static void setContext(SecurityContext context) {
        if (context == null) {
            CONTEXT_HOLDER.remove();
            return;
        }

        CONTEXT_HOLDER.set(context);
    }

    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
}
