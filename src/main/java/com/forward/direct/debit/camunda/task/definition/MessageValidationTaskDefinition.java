package com.forward.direct.debit.camunda.task.definition;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.model.InputMessage;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.TaskContext;

import java.util.Optional;

public class MessageValidationTaskDefinition extends ServiceTaskDefinition {

    public MessageValidationTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("MessageValidationTaskDefinition Executing Message Validation Task...");
        // System.out.println("Process Instance ID: " + context.getProcessInstanceId());
        parseInputMessage();
        System.out.println("=".repeat(80));
    }

    private Optional<InputMessage> parseInputMessage(String inputMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Optional.of(mapper.readValue(inputMessage, InputMessage.class));
        } catch (JsonProcessingException e) {
            System.out.println("Invalid input message format: " + e.getMessage());
            return Optional.empty();
        }
    }
}
