package com.numpty.app.task.primality;

import com.numpty.app.infra.exception.BackgroundServiceException;
import com.numpty.app.task.common.entity.PrimalityTestTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.infra.exception.ServiceException;
import com.numpty.app.task.common.entity.vo.output.PrimalityTestTaskOutput;
import com.numpty.app.task.primality.dto.PrimalityTestTaskAcceptedResponse;
import com.numpty.app.task.primality.dto.PrimalityTestTaskCreateRequest;
import com.numpty.app.task.primality.dto.PrimalityTestTaskResultUpdateRequest;
import com.numpty.app.task.primality.dto.PrimalityTestTaskStatusUpdateRequest;
import com.numpty.app.task.primality.mapper.PrimalityTestTaskMapper;
import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.web.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimalityTestTaskService {

    private static final Logger log = LoggerFactory.getLogger(PrimalityTestTaskService.class);
    private final PrimalityTestTaskMongoRepository taskMongoRepository;
    private final PrimalityTestTaskJedisRepository taskJedisRepository;
    private final PrimalityTestTaskMapper taskMapper;

    public PrimalityTestTaskService(PrimalityTestTaskMongoRepository taskMongoRepository,
        PrimalityTestTaskJedisRepository taskJedisRepository, PrimalityTestTaskMapper taskMapper) {
        this.taskMongoRepository = taskMongoRepository;
        this.taskJedisRepository = taskJedisRepository;
        this.taskMapper = taskMapper;
    }

    public PrimalityTestTaskAcceptedResponse createTask(PrimalityTestTaskCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();

        Task<PrimalityTestTaskPayload> task = taskMapper.toEntity(request, userId);

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

    private void rollbackTaskStatusToFailed(Task<PrimalityTestTaskPayload> task) {
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

    public void applyTaskWorking(PrimalityTestTaskStatusUpdateRequest request) {
        Task<PrimalityTestTaskPayload> task =  taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markWorking();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskFailed(PrimalityTestTaskStatusUpdateRequest request) {
        Task<PrimalityTestTaskPayload> task =  taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markFailed();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskUnknown(PrimalityTestTaskStatusUpdateRequest request) {
        Task<PrimalityTestTaskPayload> task =  taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markUnknown();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskCompleted(PrimalityTestTaskResultUpdateRequest request) {
        Task<PrimalityTestTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markCompleted();
        task.assignAlgorithms(request.algorithms());
        task.getTaskPayload().applyOutput(new PrimalityTestTaskOutput(request.isPrime(), request.elapsedMs()));

        taskMongoRepository.updateTaskResult(
            task.getId(),
            task.getAlgorithms(),
            task.getTaskPayload().getOutput().isPrime(),
            task.getTaskPayload().getOutput().elapsedMs(),
            task.getTaskLifecycle().getTaskStatus(),
            task.getTaskLifecycle().getUpdatedAt(),
            task.getTaskLifecycle().getCompletedAt()
        );
    }
}
