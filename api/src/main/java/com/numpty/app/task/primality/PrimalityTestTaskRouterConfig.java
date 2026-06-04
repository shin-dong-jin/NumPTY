package com.numpty.app.task.primality;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class PrimalityTestTaskRouterConfig implements RouterRegistrar {

    private final PrimalityTestTaskController taskController;

    public PrimalityTestTaskRouterConfig(PrimalityTestTaskController taskController) {
        this.taskController = taskController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry.POST("/tasks/primality-test", taskController::dispatchTask).build();
    }
}
