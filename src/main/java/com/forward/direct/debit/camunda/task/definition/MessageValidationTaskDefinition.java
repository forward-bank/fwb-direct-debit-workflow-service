package com.forward.direct.debit.camunda.task.definition;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.model.InputMessage;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;

import java.util.Optional;

public class MessageValidationTaskDefinition extends ServiceTaskDefinition {

    public MessageValidationTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("MessageValidationTaskDefinition Executing Message Validation Task...");
        // implement a method to print all the variables in executionContext for debugging
        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
        var inputMessage = (String)executionContext.getVariable("inputMessage");
        Optional<InputMessage> optionalInputMessage = parseInputMessage(inputMessage);
        System.out.println("S3 path :"+ optionalInputMessage.get().fileS3Path());
        System.out.println("Channel Ref :"+ optionalInputMessage.get().channelRef());
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
