package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.definition.MessageValidationTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;

public class MessageValidationTaskExecutor  extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition() {
        return new MessageValidationTaskDefinition();
    }

}
