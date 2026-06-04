package com.numpty.app.task.euclidean;

import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.router.RouterRegistry;

public class ExtendedEuclideanTaskRouterConfig implements RouterRegistrar {

    private final ExtendedEuclideanTaskController taskController;

    public ExtendedEuclideanTaskRouterConfig(ExtendedEuclideanTaskController taskController) {
        this.taskController = taskController;
    }

    @Override
    public RouterFunction configure(RouterRegistry registry) {
        return registry.POST("/tasks/extended-euclidean", taskController::dispatchTask).build();
    }
}
