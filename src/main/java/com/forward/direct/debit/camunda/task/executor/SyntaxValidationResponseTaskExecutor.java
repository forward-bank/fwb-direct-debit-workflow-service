package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.MessageReceiveTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.SyntaxValidationResponseTaskDefinition;

public class SyntaxValidationResponseTaskExecutor extends ReceiveTaskExecutor {

    @Override
    MessageReceiveTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new SyntaxValidationResponseTaskDefinition(executionContext);
    }

}
