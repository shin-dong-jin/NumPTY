package com.numpty.app.task.euclidean;

import com.numpty.app.task.common.entity.ExtendedEuclideanTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.task.common.Algorithm;
import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.euclidean.mapper.ExtendedEuclideanTaskMapper;
import com.numpty.framework.infra.api.MongoTemplate;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import org.bson.Document;

public class ExtendedEuclideanTaskMongoRepository {

    private static final String COLLECTION_NAME = "tasks";
    private final MongoTemplate mongoTemplate;
    private final ExtendedEuclideanTaskMapper taskMapper;

    public ExtendedEuclideanTaskMongoRepository(MongoTemplate mongoTemplate,
        ExtendedEuclideanTaskMapper taskMapper) {
        this.mongoTemplate = mongoTemplate;
        this.taskMapper = taskMapper;
    }

    public Task<ExtendedEuclideanTaskPayload> insertTask(Task<ExtendedEuclideanTaskPayload> task) {
        Document document = taskMapper.toDocument(task);

        mongoTemplate.insertOne(COLLECTION_NAME, document);

        return task;
    }

    public Task<ExtendedEuclideanTaskPayload> findTaskById(String id) {
        return mongoTemplate.findOne(COLLECTION_NAME, taskMapper.toIdFilter(id), taskMapper::toEntity);
    }

    public long updateTaskStatus(String id, TaskStatus taskStatus, Instant updatedAt) {
        return mongoTemplate.updateOne(
            COLLECTION_NAME,
            taskMapper.toIdFilter(id),
            taskMapper.toStatusUpdates(taskStatus, updatedAt)
        );
    }

    public long updateTaskResult(String id, List<Algorithm> algorithms, BigInteger gcd, BigInteger x, BigInteger y, BigDecimal elapsedMs, TaskStatus taskStatus, Instant updatedAt, Instant completedAt) {
        return mongoTemplate.updateOne(
            COLLECTION_NAME,
            taskMapper.toIdFilter(id),
            taskMapper.toTaskResultUpdates(algorithms, gcd, x, y, elapsedMs, taskStatus, updatedAt, completedAt)
        );
    }
}
