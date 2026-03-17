package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.DebulkingRequestTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;

public class DebulkingRequestTaskExecutor extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new DebulkingRequestTaskDefinition(executionContext);
    }

}
