package com.numpty.app.task.gcd;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class GcdTaskRouterConfig implements RouterRegistrar {

    private final GcdTaskController taskController;

    public GcdTaskRouterConfig(GcdTaskController taskController) {
        this.taskController = taskController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry.POST("/tasks/gcd", taskController::dispatchTask).build();
    }
}
