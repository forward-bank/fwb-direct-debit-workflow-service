package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;

public abstract class ServiceTaskExecutor implements TaskExecutor {

    @Override
    public void executeTask() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("Executing Service Task...");
        System.out.println("=".repeat(80));
    }

    abstract ServiceTaskDefinition getTaskDefinition();

}
