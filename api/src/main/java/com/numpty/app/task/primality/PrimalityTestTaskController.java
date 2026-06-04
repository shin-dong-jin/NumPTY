package com.numpty.app.task.primality;

import com.numpty.app.task.primality.dto.PrimalityTestTaskAcceptedResponse;
import com.numpty.app.task.primality.dto.PrimalityTestTaskCreateRequest;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

public class PrimalityTestTaskController {

    private final PrimalityTestTaskService taskService;

    public PrimalityTestTaskController(PrimalityTestTaskService taskService) {
        this.taskService = taskService;
    }

    public ResponseEntity<PrimalityTestTaskAcceptedResponse> dispatchTask(Request request) {
        return ResponseEntity.created().body(
            taskService.createTask(request.convertBodyToDTO(PrimalityTestTaskCreateRequest.class))
        );
    }
}
