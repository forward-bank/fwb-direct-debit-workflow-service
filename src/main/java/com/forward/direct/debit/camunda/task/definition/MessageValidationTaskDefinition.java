package com.forward.direct.debit.camunda.task.definition;


import com.forward.direct.debit.camunda.task.common.TaskContext;

public class MessageValidationTaskDefinition extends ServiceTaskDefinition {

    public MessageValidationTaskDefinition() {}

    @Override
    public void execute(TaskContext context) throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("Executing Message Validation Task...");
        System.out.println("Process Instance ID: " + context.getProcessInstanceId());
        System.out.println("=".repeat(80));
    }
}
