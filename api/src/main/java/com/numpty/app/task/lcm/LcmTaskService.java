package com.numpty.app.task.lcm;

import com.numpty.app.infra.exception.BackgroundServiceException;
import com.numpty.app.task.common.entity.LcmTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.infra.exception.ServiceException;
import com.numpty.app.task.common.entity.vo.output.LcmTaskOutput;
import com.numpty.app.task.lcm.dto.LcmTaskAcceptedResponse;
import com.numpty.app.task.lcm.dto.LcmTaskCreateRequest;
import com.numpty.app.task.lcm.dto.LcmTaskResultUpdateRequest;
import com.numpty.app.task.lcm.dto.LcmTaskStatusUpdateRequest;
import com.numpty.app.task.lcm.mapper.LcmTaskMapper;
import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.web.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LcmTaskService {

    private static final Logger log = LoggerFactory.getLogger(LcmTaskService.class);
    private final LcmTaskMongoRepository taskMongoRepository;
    private final LcmTaskJedisRepository taskJedisRepository;
    private final LcmTaskMapper taskMapper;

    public LcmTaskService(LcmTaskMongoRepository taskMongoRepository,
        LcmTaskJedisRepository taskJedisRepository, LcmTaskMapper taskMapper) {
        this.taskMongoRepository = taskMongoRepository;
        this.taskJedisRepository = taskJedisRepository;
        this.taskMapper = taskMapper;
    }

    public LcmTaskAcceptedResponse createTask(LcmTaskCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();

        Task<LcmTaskPayload> task = taskMapper.toEntity(request, userId);

        try {
            taskMongoRepository.insertTask(task);
        } catch (Exception e) {
            throw new ServiceException("Error occurred while saving task on database.", e, HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            taskJedisRepository.xAddStream(task);
        } catch (Exception e) {
            rollbackTaskStatusToFailed(task);

            throw new ServiceException("Error occurred while xAdd task on stream.", e, HttpStatus.SERVICE_UNAVAILABLE);
        }

        return taskMapper.toTaskAcceptedResponse(task);
    }

    private void rollbackTaskStatusToFailed(Task<LcmTaskPayload> task) {
        if (task == null) {
            return;
        }

        String taskId = null;

        try {
            taskId = task.getId();

            task.getTaskLifecycle().markFailed();

            taskMongoRepository.updateTaskStatus(taskId, task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
        } catch (Exception rollbackEx) {
            log.error("Fatal Error: Failed to update status to FAILED for taskId: {}", taskId, rollbackEx);
        }
    }

    public void applyTaskWorking(LcmTaskStatusUpdateRequest request) {
        Task<LcmTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markWorking();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskFailed(LcmTaskStatusUpdateRequest request) {
        Task<LcmTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markFailed();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskUnknown(LcmTaskStatusUpdateRequest request) {
        Task<LcmTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markUnknown();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskCompleted(LcmTaskResultUpdateRequest request) {
        Task<LcmTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markCompleted();
        task.assignAlgorithms(request.algorithms());
        task.getTaskPayload().applyOutput(new LcmTaskOutput(request.lcm(), request.elapsedMs()));

        taskMongoRepository.updateTaskResult(
            task.getId(),
            task.getAlgorithms(),
            task.getTaskPayload().getOutput().lcm(),
            task.getTaskPayload().getOutput().elapsedMs(),
            task.getTaskLifecycle().getTaskStatus(),
            task.getTaskLifecycle().getUpdatedAt(),
            task.getTaskLifecycle().getCompletedAt()
        );
    }
}
