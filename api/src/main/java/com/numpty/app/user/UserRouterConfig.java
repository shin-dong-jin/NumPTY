package com.numpty.app.user;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class UserRouterConfig implements RouterRegistrar {

    private final UserController userController;

    public UserRouterConfig(UserController userController) {
        this.userController = userController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry
            .POST("/auth/register", userController::register)
            .POST("/auth/login", userController::login)
            .build();
    }
}
