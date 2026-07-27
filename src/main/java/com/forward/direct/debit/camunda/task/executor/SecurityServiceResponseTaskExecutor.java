package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.MessageReceiveTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.SecurityServiceResponseTaskDefinition;

public class SecurityServiceResponseTaskExecutor extends ReceiveTaskExecutor {

    @Override
    MessageReceiveTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new SecurityServiceResponseTaskDefinition(executionContext);
    }

}
