package com.numpty.app.task.lcm.mapper;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.numpty.app.task.common.entity.LcmTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.task.common.Algorithm;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.numpty.app.task.common.TaskLifecycleMapper;
import com.numpty.app.task.lcm.dto.LcmTaskAcceptedResponse;
import com.numpty.app.task.lcm.dto.LcmTaskCreateRequest;
import com.numpty.app.task.lcm.dto.LcmTaskResultUpdateRequest;
import com.numpty.app.task.lcm.dto.LcmTaskStatusUpdateRequest;
import org.bson.Document;
import org.bson.conversions.Bson;

public class LcmTaskMapper {

    private final TaskLifecycleMapper taskLifecycleMapper;
    private final LcmTaskPayloadMapper taskPayloadMapper;

    public LcmTaskMapper(TaskLifecycleMapper taskLifecycleMapper, LcmTaskPayloadMapper taskPayloadMapper) {
        this.taskLifecycleMapper = taskLifecycleMapper;
        this.taskPayloadMapper = taskPayloadMapper;
    }

    public Task<LcmTaskPayload> toEntity(LcmTaskCreateRequest request, String userId) {
        return Task.create(request._id(), userId, taskPayloadMapper.toEntity(request));
    }

    public Task<LcmTaskPayload> toEntity(Document document) {
        return Task.reconstitute(
            document.getString(TaskKeys.TASK_ID.getValue()),
            document.getString(TaskKeys.USER_ID.getValue()),
            TaskType.valueOf(document.getString(TaskKeys.TASK_TYPE.getValue())),
            document.getList(TaskKeys.ALGORITHMS.getValue(), String.class).stream().map(Algorithm::valueOf).toList(),
            taskLifecycleMapper.toEntity(document.get(TaskKeys.TASK_LIFECYCLE.getValue(), Document.class)),
            taskPayloadMapper.toEntity(document.get(TaskKeys.TASK_PAYLOAD.getValue(), Document.class))
        );
    }

    public Document toDocument(Task<LcmTaskPayload> task) {
        return new Document()
            .append(TaskKeys.TASK_ID.getValue(), task.getId())
            .append(TaskKeys.USER_ID.getValue(), task.getUserId())
            .append(TaskKeys.TASK_TYPE.getValue(), task.getTaskType().name())
            .append(TaskKeys.ALGORITHMS.getValue(), task.getAlgorithms().stream().map(Algorithm::name).toList())
            .append(TaskKeys.TASK_LIFECYCLE.getValue(), taskLifecycleMapper.toDocument(task.getTaskLifecycle()))
            .append(TaskKeys.TASK_PAYLOAD.getValue(), taskPayloadMapper.toDocument(task.getTaskPayload()));
    }

    public Map<String, String> toRedisStreamRequestMap(Task<LcmTaskPayload> task) {
        return Map.of(
            TaskKeys.TASK_ID.getValue(), task.getId(),
            TaskKeys.TASK_STATUS.getValue(), task.getTaskLifecycle().getTaskStatus().name(),
            TaskKeys.USER_ID.getValue(), task.getUserId(),
            TaskKeys.TASK_TYPE.getValue(), task.getTaskType().name(),
            TaskKeys.TARGET_A.getValue(), task.getTaskPayload().getInput().targetA().toString(),
            TaskKeys.TARGET_B.getValue(), task.getTaskPayload().getInput().targetB().toString()
        );
    }

    public LcmTaskAcceptedResponse toTaskAcceptedResponse(Task<LcmTaskPayload> task) {
        return new LcmTaskAcceptedResponse(task.getId(), task.getTaskLifecycle().getTaskStatus());
    }

    public LcmTaskStatusUpdateRequest toTaskStatusUpdateRequest(Map<String, String> data) {
        return new LcmTaskStatusUpdateRequest(
            data.get(TaskKeys.TASK_ID.getValue()),
            TaskType.valueOf(data.get(TaskKeys.TASK_TYPE.getValue())),
            TaskStatus.fromString(data.get(TaskKeys.TASK_STATUS.getValue()))
        );
    }

    public LcmTaskResultUpdateRequest toTaskResultUpdateRequest(Map<String, String> data) {
        return new LcmTaskResultUpdateRequest(
            data.get(TaskKeys.TASK_ID.getValue()),
            Arrays.stream(data.get(TaskKeys.ALGORITHMS.getValue()).split(",")).map(Algorithm::valueOf).toList(),
            new BigInteger(data.get(TaskKeys.RESULT.getValue())),
            new BigDecimal(data.get(TaskKeys.ELAPSED_MS.getValue()))
        );
    }

    public Bson toIdFilter(String id) {
        return Filters.eq(TaskKeys.TASK_ID.getValue(), id);
    }

    public Bson toStatusUpdates(TaskStatus taskStatus, Instant updatedAt) {
        return Updates.combine(
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.TASK_STATUS.getValue(), taskStatus.name()),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.UPDATED_AT.getValue(), updatedAt)
        );
    }

    public Bson toTaskResultUpdates(List<Algorithm> algorithms, BigInteger lcm, BigDecimal elapsedMs, TaskStatus taskStatus, Instant updatedAt, Instant completedAt) {
        return Updates.combine(
            Updates.set(TaskKeys.ALGORITHMS.getValue(), algorithms.stream().map(Algorithm::name).toList()),
            Updates.set(TaskKeys.TASK_PAYLOAD.getValue() + "." + TaskKeys.OUTPUT.getValue() + "." + TaskKeys.RESULT.getValue(), lcm.toString()),
            Updates.set(TaskKeys.TASK_PAYLOAD.getValue() + "." + TaskKeys.OUTPUT.getValue() + "." + TaskKeys.ELAPSED_MS.getValue(), elapsedMs.toString()),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.TASK_STATUS.getValue(), taskStatus.name()),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.UPDATED_AT.getValue(), updatedAt),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.COMPLETED_AT.getValue(), completedAt)
        );
    }
}
