package com.numpty.app.task.primality.mapper;

import com.numpty.app.task.common.entity.PrimalityTestTaskPayload;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.entity.vo.input.PrimalityTestTaskInput;
import java.math.BigInteger;

import com.numpty.app.task.primality.dto.PrimalityTestTaskCreateRequest;
import org.bson.Document;

public class PrimalityTestTaskPayloadMapper {

    public PrimalityTestTaskPayload toEntity(PrimalityTestTaskCreateRequest taskCreateRequest) {
        return PrimalityTestTaskPayload.create(new PrimalityTestTaskInput(taskCreateRequest.target()));
    }

    public PrimalityTestTaskPayload toEntity(Document document) {
        Document input = document.get(TaskKeys.INPUT.getValue(), Document.class);

        return PrimalityTestTaskPayload.reconstitute(
            new PrimalityTestTaskInput(
                new BigInteger(input.getString(TaskKeys.TARGET.getValue()))
            )
        );
    }

    public Document toDocument(PrimalityTestTaskPayload taskPayload) {
        Document document = new Document()
            .append(TaskKeys.INPUT.getValue(),
                new Document()
                    .append(TaskKeys.TARGET.getValue(), taskPayload.getInput().target().toString()));

        if (taskPayload.getOutput() != null) {
            document.append(TaskKeys.OUTPUT.getValue(),
                new Document()
                    .append(TaskKeys.RESULT.getValue(), taskPayload.getOutput().isPrime().toString())
                    .append(TaskKeys.ELAPSED_MS.getValue(), taskPayload.getOutput().elapsedMs().toString()));
        }

        return document;
    }


}
