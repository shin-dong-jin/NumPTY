package com.numpty.app.task.factorize.mapper;

import com.numpty.app.task.common.entity.FactorizeTaskPayload;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.task.common.entity.vo.input.FactorizeTaskInput;
import java.math.BigInteger;

import com.numpty.app.task.factorize.dto.FactorizeTaskCreateRequest;
import org.bson.Document;

public class FactorizeTaskPayloadMapper {

    public FactorizeTaskPayload toEntity(FactorizeTaskCreateRequest taskCreateRequest) {
        return FactorizeTaskPayload.create(new FactorizeTaskInput(taskCreateRequest.target()));
    }

    public FactorizeTaskPayload toEntity(Document document) {
        Document input = document.get(TaskKeys.INPUT.getValue(), Document.class);

        return FactorizeTaskPayload.reconstitute(
            new FactorizeTaskInput(
                new BigInteger(input.getString(TaskKeys.TARGET.getValue()))
            )
        );
    }

    public Document toDocument(FactorizeTaskPayload taskPayload) {
        Document document = new Document()
            .append(TaskKeys.INPUT.getValue(),
                new Document()
                    .append(TaskKeys.TARGET.getValue(), taskPayload.getInput().target().toString()));

        if (taskPayload.getOutput() != null) {
            document.append(TaskKeys.OUTPUT.getValue(),
                new Document()
                    .append(TaskKeys.RESULT.getValue(), taskPayload.getOutput().factors().stream().sorted().map(BigInteger::toString).toList())
                    .append(TaskKeys.ELAPSED_MS.getValue(), taskPayload.getOutput().elapsedMs().toString()));
        }

        return document;
    }
}
