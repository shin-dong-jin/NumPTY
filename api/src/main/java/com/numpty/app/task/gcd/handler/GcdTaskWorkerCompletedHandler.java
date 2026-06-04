package com.numpty.app.task.gcd.handler;

import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.listener.ListenerHandler;
import com.numpty.app.task.gcd.GcdTaskService;
import com.numpty.app.task.gcd.mapper.GcdTaskMapper;

import java.util.Map;

public class GcdTaskWorkerCompletedHandler implements ListenerHandler {

    private final GcdTaskService taskService;
    private final GcdTaskMapper taskMapper;

    public GcdTaskWorkerCompletedHandler(GcdTaskService taskService, GcdTaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Override
    public boolean supports(Map<String, String> data) {
        return TaskType.GCD.name().equalsIgnoreCase(data.get(TaskKeys.TASK_TYPE.getValue()))
            && TaskStatus.COMPLETED.name().equalsIgnoreCase(data.get(TaskKeys.TASK_STATUS.getValue()));
    }

    @Override
    public void handle(Map<String, String> data) {
        taskService.applyTaskCompleted(taskMapper.toGcdTaskResultUpdateRequest(data));
    }
}
