package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.TaskContext;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;

public abstract class ServiceTaskExecutor implements TaskExecutor {

    @Override
    public void executeTask(TaskContext taskContext) throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("Executing Service Task...");
        getTaskDefinition().execute();
        System.out.println("=".repeat(80));
    }

    abstract ServiceTaskDefinition getTaskDefinition();

}
