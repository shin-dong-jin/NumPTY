package com.numpty.app.task.factorize.handler;

import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.listener.ListenerHandler;
import com.numpty.app.task.factorize.FactorizeTaskService;
import com.numpty.app.task.factorize.mapper.FactorizeTaskMapper;

import java.util.Map;

public class FactorizeTaskWorkerUnknownHandler implements ListenerHandler {

    private final FactorizeTaskService taskService;
    private final FactorizeTaskMapper taskMapper;

    public FactorizeTaskWorkerUnknownHandler(FactorizeTaskService taskService, FactorizeTaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Override
    public boolean supports(Map<String, String> data) {
        return TaskType.FACTORIZE.name().equalsIgnoreCase(data.get(TaskKeys.TASK_TYPE.getValue()))
                && TaskStatus.UNKNOWN.name().equalsIgnoreCase(data.get(TaskKeys.TASK_STATUS.getValue()));
    }

    @Override
    public void handle(Map<String, String> data) {
        taskService.applyTaskUnknown(taskMapper.toTaskStatusUpdateRequest(data));
    }
}
