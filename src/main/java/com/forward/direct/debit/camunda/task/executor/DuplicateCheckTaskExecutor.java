package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.DuplicateCheckTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;

public class DuplicateCheckTaskExecutor extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new DuplicateCheckTaskDefinition(executionContext);
    }

}
