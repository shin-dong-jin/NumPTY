package com.numpty.app.task.lcm.dto;

import com.numpty.app.task.common.TaskStatus;

public record LcmTaskAcceptedResponse(String id, TaskStatus taskStatus) {

}
