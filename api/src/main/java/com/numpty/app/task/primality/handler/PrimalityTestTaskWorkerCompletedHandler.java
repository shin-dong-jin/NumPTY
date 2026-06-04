package com.numpty.app.task.primality.handler;

import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.listener.ListenerHandler;
import com.numpty.app.task.primality.PrimalityTestTaskService;
import com.numpty.app.task.primality.mapper.PrimalityTestTaskMapper;

import java.util.Map;

public class PrimalityTestTaskWorkerCompletedHandler implements ListenerHandler {

    private final PrimalityTestTaskService taskService;
    private final PrimalityTestTaskMapper taskMapper;

    public PrimalityTestTaskWorkerCompletedHandler(PrimalityTestTaskService taskService,
        PrimalityTestTaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Override
    public boolean supports(Map<String, String> data) {
        return TaskType.PRIMALITY_TEST.name().equalsIgnoreCase(data.get(TaskKeys.TASK_TYPE.getValue()))
            && TaskStatus.COMPLETED.name().equalsIgnoreCase(data.get(TaskKeys.TASK_STATUS.getValue()));
    }

    @Override
    public void handle(Map<String, String> data) {
        taskService.applyTaskCompleted(taskMapper.toTaskResultUpdateRequest(data));
    }
}
