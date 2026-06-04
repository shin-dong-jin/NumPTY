package com.numpty.framework.web.api;

import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public interface RouterRegistrar {

    RouterFunction configure(RouterRegistry registry);
}
