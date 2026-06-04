package com.numpty.app.task.gcd.mapper;

import com.numpty.app.task.common.entity.GcdTaskPayload;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.entity.vo.input.GcdTaskInput;

import java.math.BigInteger;

import com.numpty.app.task.gcd.dto.GcdTaskCreateRequest;
import org.bson.Document;

public class GcdTaskPayloadMapper {

    public GcdTaskPayload toEntity(GcdTaskCreateRequest request) {
        return GcdTaskPayload.create(new GcdTaskInput(request.targetA(), request.targetB()));
    }

    public GcdTaskPayload toEntity(Document document) {
        Document input = document.get(TaskKeys.INPUT.getValue(), Document.class);

        return GcdTaskPayload.reconstitute(
                new GcdTaskInput(
                        new BigInteger(input.getString(TaskKeys.TARGET_A.getValue())),
                        new BigInteger(input.getString(TaskKeys.TARGET_B.getValue()))
                )
        );
    }

    public Document toDocument(GcdTaskPayload taskPayload) {
        Document document = new Document()
                .append(TaskKeys.INPUT.getValue(),
                        new Document()
                                .append(TaskKeys.TARGET_A.getValue(), taskPayload.getInput().targetA().toString())
                                .append(TaskKeys.TARGET_B.getValue(), taskPayload.getInput().targetB().toString()));

        if (taskPayload.getOutput() != null) {
            document.append(TaskKeys.OUTPUT.getValue(),
                    new Document()
                            .append(TaskKeys.RESULT.getValue(), taskPayload.getOutput().gcd().toString())
                            .append(TaskKeys.ELAPSED_MS.getValue(), taskPayload.getOutput().elapsedMs().toString()));
        }

        return document;
    }
}
