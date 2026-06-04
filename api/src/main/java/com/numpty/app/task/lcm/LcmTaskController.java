package com.numpty.app.task.lcm;

import com.numpty.app.task.lcm.dto.LcmTaskAcceptedResponse;
import com.numpty.app.task.lcm.dto.LcmTaskCreateRequest;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

public class LcmTaskController {

    private final LcmTaskService taskService;

    public LcmTaskController(LcmTaskService taskService) {
        this.taskService = taskService;
    }

    public ResponseEntity<LcmTaskAcceptedResponse> dispatchTask(Request request) {
        return ResponseEntity.created().body(
            taskService.createTask(request.convertBodyToDTO(LcmTaskCreateRequest.class))
        );
    }
}
