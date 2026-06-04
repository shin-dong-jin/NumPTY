package com.numpty.framework.security.web.chain;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface SecurityFilterChain extends Ordered {

    boolean matches(HttpServletRequest request);
    List<Filter> getFilters();
}
