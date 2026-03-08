package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.SyntaxValidationResponseTaskDefinition;

public class SyntaxValidationResponseTaskExecutor extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new SyntaxValidationResponseTaskDefinition(executionContext);
    }

}
