package com.numpty.app.task.lcm;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class LcmTaskRouterConfig implements RouterRegistrar {

    private final LcmTaskController taskController;

    public LcmTaskRouterConfig(LcmTaskController taskController) {
        this.taskController = taskController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry.POST("/tasks/lcm", taskController::dispatchTask).build();
    }
}
