package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
// import com.forward.direct.debit.camunda.task.common.TaskContext;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;
import org.springframework.context.ApplicationContext;

public abstract class ServiceTaskExecutor implements TaskExecutor {

    @Override
    public void executeTask(ExecutionContext executionContext, ApplicationContext applicationContext) throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("ServiceTaskExecutor executeTask()...");
        getTaskDefinition(executionContext, applicationContext).execute();
        System.out.println("=".repeat(80));
    }

    abstract ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext,
                                                     ApplicationContext applicationContext);

}
