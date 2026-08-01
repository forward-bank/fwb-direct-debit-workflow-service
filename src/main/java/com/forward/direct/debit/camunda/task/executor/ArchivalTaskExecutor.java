package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.ArchivalTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;
import org.springframework.context.ApplicationContext;

public class ArchivalTaskExecutor extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext, ApplicationContext applicationContext) {
        return new ArchivalTaskDefinition(executionContext, applicationContext);
    }

}
