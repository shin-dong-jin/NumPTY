package com.numpty.app.task.gcd;

import com.numpty.app.infra.exception.BackgroundServiceException;
import com.numpty.app.task.common.entity.GcdTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.infra.exception.ServiceException;
import com.numpty.app.task.common.entity.vo.output.GcdTaskOutput;
import com.numpty.app.task.gcd.dto.GcdTaskCreateRequest;
import com.numpty.app.task.gcd.dto.GcdTaskResultUpdateRequest;
import com.numpty.app.task.gcd.dto.GcdTaskStatusUpdateRequest;
import com.numpty.app.task.gcd.dto.GcdTaskAcceptedResponse;
import com.numpty.app.task.gcd.mapper.GcdTaskMapper;
import com.numpty.framework.security.core.authentication.Authentication;
import com.numpty.framework.security.core.context.SecurityContextHolder;
import com.numpty.framework.web.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcdTaskService {

    private static final Logger log = LoggerFactory.getLogger(GcdTaskService.class);
    private final GcdTaskMongoRepository taskMongoRepository;
    private final GcdTaskJedisRepository taskJedisRepository;
    private final GcdTaskMapper taskMapper;

    public GcdTaskService(GcdTaskMongoRepository taskMongoRepository,
        GcdTaskJedisRepository taskJedisRepository, GcdTaskMapper taskMapper) {
        this.taskMongoRepository = taskMongoRepository;
        this.taskJedisRepository = taskJedisRepository;
        this.taskMapper = taskMapper;
    }

    public GcdTaskAcceptedResponse createTask(GcdTaskCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();

        Task<GcdTaskPayload> task = taskMapper.toEntity(request, userId);

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

    private void rollbackTaskStatusToFailed(Task<GcdTaskPayload> task) {
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

    public void applyTaskWorking(GcdTaskStatusUpdateRequest request) {
        Task<GcdTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markWorking();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskFailed(GcdTaskStatusUpdateRequest request) {
        Task<GcdTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markFailed();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskUnknown(GcdTaskStatusUpdateRequest request) {
        Task<GcdTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markUnknown();

        taskMongoRepository.updateTaskStatus(task.getId(), task.getTaskLifecycle().getTaskStatus(), task.getTaskLifecycle().getUpdatedAt());
    }

    public void applyTaskCompleted(GcdTaskResultUpdateRequest request) {
        Task<GcdTaskPayload> task = taskMongoRepository.findTaskById(request.id());

        if (task == null) {
            throw new BackgroundServiceException("Failed to find task. id: " + request.id());
        }

        task.getTaskLifecycle().markCompleted();
        task.assignAlgorithms(request.algorithms());
        task.getTaskPayload().applyOutput(new GcdTaskOutput(request.gcd(), request.elapsedMs()));

        taskMongoRepository.updateTaskResult(
            task.getId(),
            task.getAlgorithms(),
            task.getTaskPayload().getOutput().gcd(),
            task.getTaskPayload().getOutput().elapsedMs(),
            task.getTaskLifecycle().getTaskStatus(),
            task.getTaskLifecycle().getUpdatedAt(),
            task.getTaskLifecycle().getCompletedAt()
        );
    }
}
