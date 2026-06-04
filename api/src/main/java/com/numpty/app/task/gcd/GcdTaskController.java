package com.numpty.app.task.gcd;

import com.numpty.app.task.gcd.dto.GcdTaskCreateRequest;
import com.numpty.app.task.gcd.dto.GcdTaskAcceptedResponse;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

public class GcdTaskController {

    private final GcdTaskService taskService;

    public GcdTaskController(GcdTaskService taskService) {
        this.taskService = taskService;
    }

    public ResponseEntity<GcdTaskAcceptedResponse> dispatchTask(Request request) {
        return ResponseEntity.created().body(
            taskService.createTask(request.convertBodyToDTO(GcdTaskCreateRequest.class))
        );
    }
}
