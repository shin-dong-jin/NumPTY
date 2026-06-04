package com.numpty.app.task.lcm.mapper;

import com.numpty.app.task.common.entity.LcmTaskPayload;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.entity.vo.input.LcmTaskInput;
import java.math.BigInteger;

import com.numpty.app.task.lcm.dto.LcmTaskCreateRequest;
import org.bson.Document;

public class LcmTaskPayloadMapper {

    public LcmTaskPayload toEntity(LcmTaskCreateRequest request) {
        return LcmTaskPayload.create(new LcmTaskInput(request.targetA(), request.targetB()));
    }

    public LcmTaskPayload toEntity(Document document) {
        Document input = document.get(TaskKeys.INPUT.getValue(), Document.class);

        return LcmTaskPayload.reconstitute(
            new LcmTaskInput(
                new BigInteger(input.getString(TaskKeys.TARGET_A.getValue())),
                new BigInteger(input.getString(TaskKeys.TARGET_B.getValue()))
            )
        );
    }

    public Document toDocument(LcmTaskPayload taskPayload) {
        Document document = new Document()
            .append(TaskKeys.INPUT.getValue(),
                new Document()
                    .append(TaskKeys.TARGET_A.getValue(), taskPayload.getInput().targetA().toString())
                    .append(TaskKeys.TARGET_B.getValue(), taskPayload.getInput().targetB().toString()));

        if (taskPayload.getOutput() != null) {
            document.append(TaskKeys.OUTPUT.getValue(),
                new Document()
                    .append(TaskKeys.RESULT.getValue(), taskPayload.getOutput().lcm().toString())
                    .append(TaskKeys.ELAPSED_MS.getValue(), taskPayload.getOutput().elapsedMs().toString()));
        }

        return document;
    }


}
