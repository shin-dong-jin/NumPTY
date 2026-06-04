package com.numpty.app.task.factorize;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class FactorizeTaskRouterConfig implements RouterRegistrar {

    private final FactorizeTaskController taskController;

    public FactorizeTaskRouterConfig(FactorizeTaskController taskController) {
        this.taskController = taskController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry.POST("/tasks/factorize", taskController::dispatchTask).build();
    }
}
