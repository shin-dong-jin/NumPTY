package com.numpty.app.task.factorize.dto;

import com.numpty.app.task.common.TaskStatus;

public record FactorizeTaskAcceptedResponse(String id, TaskStatus taskStatus) {

}
