package com.numpty.app.main;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class MainRouterConfig implements RouterRegistrar {

    private final MainController mainController;

    public MainRouterConfig(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry
                .GET("/", mainController::view)
                .GET("/index", mainController::view)
                .GET("/favicon.ico", mainController::favicon)
                .GET("/health-check", mainController::healthCheck)
                .build();
    }
}
