package com.forward.direct.debit.camunda.task.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;

public class DebulkingRequestTaskDefinition extends ServiceTaskDefinition{

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public DebulkingRequestTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("DebulkingRequestTaskDefinition Executing Debulking Request Task...");
        // implement a method to print all the variables in executionContext for debugging
        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });

        System.out.println("=".repeat(80));

    }

}
