package com.numpty.app.task.euclidean;

import com.numpty.app.task.euclidean.dto.ExtendedEuclideanTaskAcceptedResponse;
import com.numpty.app.task.euclidean.dto.ExtendedEuclideanTaskCreateRequest;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;

public class ExtendedEuclideanTaskController {

    private final ExtendedEuclideanTaskService taskService;

    public ExtendedEuclideanTaskController(ExtendedEuclideanTaskService taskService) {
        this.taskService = taskService;
    }

    public ResponseEntity<ExtendedEuclideanTaskAcceptedResponse> dispatchTask(Request request) {
        return ResponseEntity.created().body(
            taskService.createTask(request.convertBodyToDTO(ExtendedEuclideanTaskCreateRequest.class))
        );
    }
}
