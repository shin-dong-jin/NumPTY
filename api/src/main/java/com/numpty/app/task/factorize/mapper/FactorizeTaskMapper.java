package com.numpty.app.task.factorize.mapper;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.numpty.app.task.common.entity.FactorizeTaskPayload;
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
import com.numpty.app.task.factorize.dto.FactorizeTaskAcceptedResponse;
import com.numpty.app.task.factorize.dto.FactorizeTaskCreateRequest;
import com.numpty.app.task.factorize.dto.FactorizeTaskResultUpdateRequest;
import com.numpty.app.task.factorize.dto.FactorizeTaskStatusUpdateRequest;
import org.bson.Document;
import org.bson.conversions.Bson;

public class FactorizeTaskMapper {

    private final TaskLifecycleMapper taskLifecycleMapper;
    private final FactorizeTaskPayloadMapper taskPayloadMapper;

    public FactorizeTaskMapper(TaskLifecycleMapper taskLifecycleMapper, FactorizeTaskPayloadMapper taskPayloadMapper) {
        this.taskLifecycleMapper = taskLifecycleMapper;
        this.taskPayloadMapper = taskPayloadMapper;
    }

    public Task<FactorizeTaskPayload> toEntity(FactorizeTaskCreateRequest createRequest, String userId) {
        return Task.create(createRequest._id(), userId, taskPayloadMapper.toEntity(createRequest));
    }

    public Task<FactorizeTaskPayload> toEntity(Document document) {
        return Task.reconstitute(
            document.getString(TaskKeys.TASK_ID.getValue()),
            document.getString(TaskKeys.USER_ID.getValue()),
            TaskType.valueOf(document.getString(TaskKeys.TASK_TYPE.getValue())),
            document.getList(TaskKeys.ALGORITHMS.getValue(), String.class).stream().map(Algorithm::valueOf).toList(),
            taskLifecycleMapper.toEntity(document.get(TaskKeys.TASK_LIFECYCLE.getValue(), Document.class)),
            taskPayloadMapper.toEntity(document.get(TaskKeys.TASK_PAYLOAD.getValue(), Document.class))
        );
    }

    public Document toDocument(Task<FactorizeTaskPayload> task) {
        return new Document()
            .append(TaskKeys.TASK_ID.getValue(), task.getId())
            .append(TaskKeys.USER_ID.getValue(), task.getUserId())
            .append(TaskKeys.TASK_TYPE.getValue(), task.getTaskType().name())
            .append(TaskKeys.ALGORITHMS.getValue(), task.getAlgorithms().stream().map(Algorithm::name).toList())
            .append(TaskKeys.TASK_LIFECYCLE.getValue(), taskLifecycleMapper.toDocument(task.getTaskLifecycle()))
            .append(TaskKeys.TASK_PAYLOAD.getValue(), taskPayloadMapper.toDocument(task.getTaskPayload()));
    }

    public Map<String, String> toRedisStreamRequestMap(Task<FactorizeTaskPayload> task) {
        return Map.of(
            TaskKeys.TASK_ID.getValue(), task.getId(),
            TaskKeys.TASK_STATUS.getValue(), task.getTaskLifecycle().getTaskStatus().name(),
            TaskKeys.USER_ID.getValue(), task.getUserId(),
            TaskKeys.TASK_TYPE.getValue(), task.getTaskType().name(),
            TaskKeys.TARGET.getValue(), task.getTaskPayload().getInput().target().toString()
        );
    }

    public FactorizeTaskAcceptedResponse toTaskAcceptedResponse(Task<FactorizeTaskPayload> task) {
        return new FactorizeTaskAcceptedResponse(task.getId(), task.getTaskLifecycle().getTaskStatus());
    }

    public FactorizeTaskStatusUpdateRequest toTaskStatusUpdateRequest(Map<String, String> data) {
        return new FactorizeTaskStatusUpdateRequest(
            data.get(TaskKeys.TASK_ID.getValue()),
            TaskType.valueOf(data.get(TaskKeys.TASK_TYPE.getValue())),
            TaskStatus.fromString(data.get(TaskKeys.TASK_STATUS.getValue()))
        );
    }

    public FactorizeTaskResultUpdateRequest toTaskResultUpdateRequest(Map<String, String> data) {
        return new FactorizeTaskResultUpdateRequest(
            data.get(TaskKeys.TASK_ID.getValue()),
            Arrays.stream(data.get(TaskKeys.ALGORITHMS.getValue()).split(",")).map(Algorithm::valueOf).toList(),
            Arrays.stream(data.get(TaskKeys.RESULT.getValue()).split("\\*")).map(BigInteger::new).sorted().toList(),
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

    public Bson toTaskResultUpdates(List<Algorithm> algorithms, List<BigInteger> factors, BigDecimal elapsedMs, TaskStatus taskStatus, Instant updatedAt, Instant completedAt) {
        return Updates.combine(
            Updates.set(TaskKeys.ALGORITHMS.getValue(), algorithms.stream().map(Algorithm::name).toList()),
            Updates.set(TaskKeys.TASK_PAYLOAD.getValue() + "." + TaskKeys.OUTPUT.getValue() + "." + TaskKeys.RESULT.getValue(), factors.stream().sorted().map(String::valueOf).toList()),
            Updates.set(TaskKeys.TASK_PAYLOAD.getValue() + "." + TaskKeys.OUTPUT.getValue() + "." + TaskKeys.ELAPSED_MS.getValue(), elapsedMs.toString()),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.TASK_STATUS.getValue(), taskStatus.name()),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.UPDATED_AT.getValue(), updatedAt),
            Updates.set(TaskKeys.TASK_LIFECYCLE.getValue() + "." + TaskKeys.COMPLETED_AT.getValue(), completedAt)
        );
    }
}
