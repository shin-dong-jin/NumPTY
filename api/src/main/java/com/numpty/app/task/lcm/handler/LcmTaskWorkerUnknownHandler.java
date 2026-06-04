package com.numpty.app.task.lcm.handler;

import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.listener.ListenerHandler;
import com.numpty.app.task.lcm.mapper.LcmTaskMapper;
import com.numpty.app.task.lcm.LcmTaskService;

import java.util.Map;

public class LcmTaskWorkerUnknownHandler implements ListenerHandler {

    private final LcmTaskService taskService;
    private final LcmTaskMapper taskMapper;

    public LcmTaskWorkerUnknownHandler(LcmTaskService taskService, LcmTaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Override
    public boolean supports(Map<String, String> data) {
        return TaskType.LCM.name().equalsIgnoreCase(data.get(TaskKeys.TASK_TYPE.getValue()))
                && TaskStatus.UNKNOWN.name().equalsIgnoreCase(data.get(TaskKeys.TASK_STATUS.getValue()));
    }

    @Override
    public void handle(Map<String, String> data) {
        taskService.applyTaskUnknown(taskMapper.toTaskStatusUpdateRequest(data));
    }
}
