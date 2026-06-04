package com.numpty.app.task.primality;

import com.numpty.app.task.common.entity.PrimalityTestTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.task.common.Algorithm;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.primality.mapper.PrimalityTestTaskMapper;
import com.numpty.framework.infra.api.MongoTemplate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.bson.Document;

public class PrimalityTestTaskMongoRepository {

    private static final String COLLECTION_NAME = "tasks";
    private final MongoTemplate mongoTemplate;
    private final PrimalityTestTaskMapper taskMapper;

    public PrimalityTestTaskMongoRepository(MongoTemplate mongoTemplate,
        PrimalityTestTaskMapper taskMapper) {
        this.mongoTemplate = mongoTemplate;
        this.taskMapper = taskMapper;
    }

    public Task<PrimalityTestTaskPayload> insertTask(Task<PrimalityTestTaskPayload> task) {
        Document document = taskMapper.toDocument(task);

        mongoTemplate.insertOne(COLLECTION_NAME, document);

        return task;
    }

    public Task<PrimalityTestTaskPayload> findTaskById(String id) {
        return mongoTemplate.findOne(COLLECTION_NAME, taskMapper.toIdFilter(id), taskMapper::toEntity);
    }

    public long updateTaskStatus(String id, TaskStatus taskStatus, Instant updatedAt) {
        return mongoTemplate.updateOne(
            COLLECTION_NAME,
            taskMapper.toIdFilter(id),
            taskMapper.toStatusUpdates(taskStatus, updatedAt)
        );
    }

    public long updateTaskResult(String id, List<Algorithm> algorithms, Boolean isPrime, BigDecimal elapsedMs, TaskStatus taskStatus, Instant updatedAt, Instant completedAt) {
        return mongoTemplate.updateOne(
            COLLECTION_NAME,
            taskMapper.toIdFilter(id),
            taskMapper.toTaskResultUpdates(algorithms, isPrime, elapsedMs, taskStatus, updatedAt, completedAt)
        );
    }
}
