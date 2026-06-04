package com.numpty.app.task.factorize;

import com.numpty.app.infra.exception.BackgroundServiceException;
import com.numpty.app.task.common.entity.FactorizeTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.infra.exception.ServiceException;
import com.numpty.app.task.common.entity.vo.output.FactorizeTaskOutput;
import com.numpty.app.task.factorize.dto.FactorizeTaskAcceptedResponse;
import com.numpty.app.task.factorize.dto.FactorizeTaskCreateRequest;
import com.numpty.app.task.factorize.dto.FactorizeTaskResultUpdateRequest;
import com.numpty.app.task.factorize.dto.FactorizeTaskStatusUpdateRequest;
import com.numpty.app.task.factorize.mapper.FactorizeTaskMapper;
import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.web.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactorizeTaskService {

    private static final Logger log = LoggerFactory.getLogger(FactorizeTaskService.class);
    private final FactorizeTaskMongoRepository taskMongoRepository;
    private final FactorizeTaskJedisRepository taskJedisRepository;
    private final FactorizeTaskMapper taskMapper;

    public FactorizeTaskService(FactorizeTaskMongoRepository taskMongoRepository,
        FactorizeTaskJedisRepository taskJedisRepository, FactorizeTaskMapper taskMapper) {
        this.taskMongoRepository = taskMongoRepository;
        this.taskJedisRepository = taskJedisRepository;
        this.taskMapper = taskMapper;
    }

    public FactorizeTaskAcceptedResponse createTask(FactorizeTaskCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();

        Task<FactorizeTaskPayload> task = taskMapper.toEntity(request, userId);

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

    private void rollbackTaskStatusToFailed(Task<FactorizeTaskPayload> task) {
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

    public void applyTaskWorking(FactorizeTaskStatusUpdateRequest request) {
        Task<FactorizeTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markWorking();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskFailed(FactorizeTaskStatusUpdateRequest request) {
        Task<FactorizeTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markFailed();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskUnknown(FactorizeTaskStatusUpdateRequest request) {
        Task<FactorizeTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markUnknown();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskCompleted(FactorizeTaskResultUpdateRequest request) {
        Task<FactorizeTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markCompleted();
        task.assignAlgorithms(request.algorithms());
        task.getTaskPayload().applyOutput(new FactorizeTaskOutput(request.factors(), request.elapsedMs()));

        taskMongoRepository.updateTaskResult(
            task.getId(),
            task.getAlgorithms(),
            task.getTaskPayload().getOutput().factors(),
            task.getTaskPayload().getOutput().elapsedMs(),
            task.getTaskLifecycle().getTaskStatus(),
            task.getTaskLifecycle().getUpdatedAt(),
            task.getTaskLifecycle().getCompletedAt()
        );
    }
}
