package com.numpty.app.task.gcd.dto;

import com.numpty.app.task.common.TaskStatus;

public record GcdTaskAcceptedResponse(String id, TaskStatus taskStatus) {

}
