package com.forward.direct.debit.camunda.task.definition;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;

public class CustomerValidationTaskDefinition extends ServiceTaskDefinition{

    public CustomerValidationTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }
    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("CustomerValidationTaskDefinition Executing Customer Validation Task...");
        // implement a method to print all the variables in executionContext for debugging
        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
        System.out.println("=".repeat(80));
    }
}
