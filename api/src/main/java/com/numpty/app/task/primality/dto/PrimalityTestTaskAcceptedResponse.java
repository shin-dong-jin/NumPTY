package com.numpty.app.task.primality.dto;

import com.numpty.app.task.common.TaskStatus;

public record PrimalityTestTaskAcceptedResponse(String id, TaskStatus taskStatus) {

}
