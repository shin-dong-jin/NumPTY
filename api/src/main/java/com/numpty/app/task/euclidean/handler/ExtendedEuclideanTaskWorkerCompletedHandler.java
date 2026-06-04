package com.numpty.app.task.euclidean.handler;

import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.listener.ListenerHandler;
import com.numpty.app.task.euclidean.ExtendedEuclideanTaskService;
import com.numpty.app.task.euclidean.mapper.ExtendedEuclideanTaskMapper;

import java.util.Map;

public class ExtendedEuclideanTaskWorkerCompletedHandler implements ListenerHandler {

    private final ExtendedEuclideanTaskService taskService;
    private final ExtendedEuclideanTaskMapper taskMapper;

    public ExtendedEuclideanTaskWorkerCompletedHandler(ExtendedEuclideanTaskService taskService,
        ExtendedEuclideanTaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Override
    public boolean supports(Map<String, String> data) {
        return TaskType.EXTENDED_GCD.name().equalsIgnoreCase(data.get(TaskKeys.TASK_TYPE.getValue()))
            && TaskStatus.COMPLETED.name().equalsIgnoreCase(data.get(TaskKeys.TASK_STATUS.getValue()));
    }

    @Override
    public void handle(Map<String, String> data) {
        taskService.applyTaskCompleted(taskMapper.toTaskResultUpdateRequest(data));
    }
}
