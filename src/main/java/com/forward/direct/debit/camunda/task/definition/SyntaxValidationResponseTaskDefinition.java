package com.forward.direct.debit.camunda.task.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;

import javax.jms.Message;

public class SyntaxValidationResponseTaskDefinition extends MessageReceiveTaskDefinition{

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SyntaxValidationResponseTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }

    @Override
    public void execute(Message message) throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("SyntaxValidationResponseTaskDefinition Executing Syntax Validation Response Task...");
        // implement a method to print all the variables in executionContext for debugging
        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
        // implement a method to print the message content for debugging
        System.out.println("Received JMS Message:");
        String messageContent = message.getBody(String.class);
        System.out.println(messageContent);

        System.out.println("=".repeat(80));
    }
}
