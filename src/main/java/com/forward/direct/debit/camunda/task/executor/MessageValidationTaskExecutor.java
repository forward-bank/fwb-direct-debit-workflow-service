package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.TaskContext;
import com.forward.direct.debit.camunda.task.definition.MessageValidationTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;

public class MessageValidationTaskExecutor  extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new MessageValidationTaskDefinition(executionContext);
    }

}
