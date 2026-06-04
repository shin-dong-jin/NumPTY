package com.numpty.app.task.primality.mapper;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.numpty.app.task.common.entity.PrimalityTestTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.task.common.Algorithm;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.numpty.app.task.common.TaskLifecycleMapper;
import com.numpty.app.task.primality.dto.PrimalityTestTaskAcceptedResponse;
import com.numpty.app.task.primality.dto.PrimalityTestTaskCreateRequest;
import com.numpty.app.task.primality.dto.PrimalityTestTaskResultUpdateRequest;
import com.numpty.app.task.primality.dto.PrimalityTestTaskStatusUpdateRequest;
import org.bson.Document;
import org.bson.conversions.Bson;

public class PrimalityTestTaskMapper {

    private final TaskLifecycleMapper taskLifecycleMapper;
    private final PrimalityTestTaskPayloadMapper taskPayloadMapper;

    public PrimalityTestTaskMapper(TaskLifecycleMapper taskLifecycleMapper, PrimalityTestTaskPayloadMapper taskPayloadMapper) {
        this.taskLifecycleMapper = taskLifecycleMapper;
        this.taskPayloadMapper = taskPayloadMapper;
    }

    public Task<PrimalityTestTaskPayload> toEntity(PrimalityTestTaskCreateRequest request, String userId) {
        return Task.create(request._id(), userId, taskPayloadMapper.toEntity(request));
    }

    public Task<PrimalityTestTaskPayload> toEntity(Document document) {
        return Task.reconstitute(
            document.getString(TaskKeys.TASK_ID.getValue()),
            document.getString(TaskKeys.USER_ID.getValue()),
            TaskType.valueOf(document.getString(TaskKeys.TASK_TYPE.getValue())),
            document.getList(TaskKeys.ALGORITHMS.getValue(), String.class).stream().map(Algorithm::valueOf).toList(),
            taskLifecycleMapper.toEntity(document.get(TaskKeys.TASK_LIFECYCLE.getValue(), Document.class)),
            taskPayloadMapper.toEntity(document.get(TaskKeys.TASK_PAYLOAD.getValue(), Document.class))
        );
    }

    public Document toDocument(Task<PrimalityTestTaskPayload> task) {
        return new Document()
            .append(TaskKeys.TASK_ID.getValue(), task.getId())
            .append(TaskKeys.USER_ID.getValue(), task.getUserId())
            .append(TaskKeys.TASK_TYPE.getValue(), task.getTaskType().name())
            .append(TaskKeys.ALGORITHMS.getValue(), task.getAlgorithms().stream().map(Algorithm::name).toList())
            .append(TaskKeys.TASK_LIFECYCLE.getValue(), taskLifecycleMapper.toDocument(task.getTaskLifecycle()))
            .append(TaskKeys.TASK_PAYLOAD.getValue(), taskPayloadMapper.toDocument(task.getTaskPayload()));
    }

    public Map<String, String> toRedisStreamRequestMap(Task<PrimalityTestTaskPayload> task) {
        return Map.of(
            TaskKeys.TASK_ID.getValue(), task.getId(),
            TaskKeys.TASK_STATUS.getValue(), task.getTaskLifecycle().getTaskStatus().name(),
            TaskKeys.USER_ID.getValue(), task.getUserId(),
            TaskKeys.TASK_TYPE.getValue(), task.getTaskType().name(),
            TaskKeys.TARGET.getValue(), task.getTaskPayload().getInput().target().toString()
        );
    }

    public PrimalityTestTaskAcceptedResponse toTaskAcceptedResponse(Task<PrimalityTestTaskPayload> task) {
        return new PrimalityTestTaskAcceptedResponse(task.getId(), task.getTaskLifecycle().getTaskStatus());
    }

    public PrimalityTestTaskStatusUpdateRequest toTaskStatusUpdateRequest(Map<String, String> data) {
        return new PrimalityTestTaskStatusUpdateRequest(
            data.get(TaskKeys.TASK_ID.getValue()),
            TaskType.valueOf(data.get(TaskKeys.TASK_TYPE.getValue())),
            TaskStatus.fromString(data.get(TaskKeys.TASK_STATUS.getValue()))
        );
    }

    public PrimalityTestTaskResultUpdateRequest toTaskResultUpdateRequest(Map<String, String> data) {
        return new PrimalityTestTaskResultUpdateRequest(
            data.get(TaskKeys.TASK_ID.getValue()),
            Arrays.stream(data.get(TaskKeys.ALGORITHMS.getValue()).split(",")).map(Algorithm::valueOf).toList(),
            Boolean.parseBoolean(data.get(TaskKeys.RESULT.getValue())),
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

    public Bson toTaskResultUpdates(List<Algorithm> algorithms, Boolean isPrime, BigDecimal elapsedMs, TaskStatus taskStatus, Instant updatedAt, Instant completedAt) {
        return Updates.combine(
            Updates.set(TaskKeys.ALGORITHMS.getValue(), algorithms.stream().map(Algorithm::name).toList()),
            Updates.set(TaskKeys.TASK_PAYLOAD.getValue() + "." + TaskKeys.OUTPUT.getValue() + "." + TaskKeys.RESULT.getValue(), isPrime),
            Updates.set(TaskKeys.TASK_PAYLOAD.getValue() + "." + TaskKeys.OUTPUT.getValue() + "." + TaskKeys.ELAPSED_MS.getValue(), elapsedMs.toString()),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.TASK_STATUS.getValue(), taskStatus.name()),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.UPDATED_AT.getValue(), updatedAt),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.COMPLETED_AT.getValue(), completedAt)
        );
    }
}
