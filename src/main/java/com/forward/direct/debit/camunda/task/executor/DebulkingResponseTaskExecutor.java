package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.DebulkingResponseTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.MessageReceiveTaskDefinition;

public class DebulkingResponseTaskExecutor extends ReceiveTaskExecutor {

    @Override
    MessageReceiveTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new DebulkingResponseTaskDefinition(executionContext);
    }

}
