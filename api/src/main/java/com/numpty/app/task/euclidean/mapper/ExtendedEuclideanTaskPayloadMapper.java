package com.numpty.app.task.euclidean.mapper;

import com.numpty.app.task.common.entity.ExtendedEuclideanTaskPayload;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.entity.vo.input.ExtendedEuclideanTaskInput;
import java.math.BigInteger;

import com.numpty.app.task.euclidean.dto.ExtendedEuclideanTaskCreateRequest;
import org.bson.Document;

public class ExtendedEuclideanTaskPayloadMapper {

    public ExtendedEuclideanTaskPayload toEntity(
        ExtendedEuclideanTaskCreateRequest taskCreateRequest) {
        return ExtendedEuclideanTaskPayload.create(new ExtendedEuclideanTaskInput(taskCreateRequest.targetA(), taskCreateRequest.targetB()));
    }

    public ExtendedEuclideanTaskPayload toEntity(Document document) {
        Document input = document.get(TaskKeys.INPUT.getValue(), Document.class);

        return ExtendedEuclideanTaskPayload.reconstitute(
            new ExtendedEuclideanTaskInput(
                new BigInteger(input.getString(TaskKeys.TARGET_A.getValue())),
                new BigInteger(input.getString(TaskKeys.TARGET_B.getValue()))
            )
        );
    }

    public Document toDocument(ExtendedEuclideanTaskPayload extendedEuclideanTaskPayload) {
        Document document = new Document()
            .append(TaskKeys.INPUT.getValue(),
                new Document()
                    .append(TaskKeys.TARGET_A.getValue(), extendedEuclideanTaskPayload.getInput().targetA().toString())
                    .append(TaskKeys.TARGET_B.getValue(), extendedEuclideanTaskPayload.getInput().targetB().toString()));

        if (extendedEuclideanTaskPayload.getOutput() != null) {
            document.append(TaskKeys.OUTPUT.getValue(),
                new Document()
                    .append(TaskKeys.RESULT.getValue(), extendedEuclideanTaskPayload.getOutput().gcd().toString())
                    .append(TaskKeys.RESULT_X.getValue(), extendedEuclideanTaskPayload.getOutput().x().toString())
                    .append(TaskKeys.RESULT_Y.getValue(), extendedEuclideanTaskPayload.getOutput().y().toString())
                    .append(TaskKeys.ELAPSED_MS.getValue(), extendedEuclideanTaskPayload.getOutput().elapsedMs().toString()));
        }

        return document;
    }


}
