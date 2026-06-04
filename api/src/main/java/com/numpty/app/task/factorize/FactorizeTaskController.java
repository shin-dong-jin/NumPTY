package com.numpty.app.task.factorize;

import com.numpty.app.task.factorize.dto.FactorizeTaskAcceptedResponse;
import com.numpty.app.task.factorize.dto.FactorizeTaskCreateRequest;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

public class FactorizeTaskController {

    private final FactorizeTaskService taskService;

    public FactorizeTaskController(FactorizeTaskService taskService) {
        this.taskService = taskService;
    }

    public ResponseEntity<FactorizeTaskAcceptedResponse> dispatchTask(Request request) {
        return ResponseEntity.created().body(
            taskService.createTask(request.convertBodyToDTO(FactorizeTaskCreateRequest.class))
        );
    }
}
