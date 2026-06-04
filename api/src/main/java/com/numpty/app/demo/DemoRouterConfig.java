package com.numpty.app.demo;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class DemoRouterConfig implements RouterRegistrar {
    private final DemoController demoController;

    public DemoRouterConfig(DemoController demoController) {
        this.demoController = demoController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry
                .GET("/demo", demoController::demo)
                .GET("/demo-path/{id}/{query}", demoController::demoPath)
                .POST("/demo", demoController::demoPost)
                .build();
    }
}
