package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.CustomerValidationTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;

public class CustomerValidationTaskExecutor extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext) {
        return new CustomerValidationTaskDefinition(executionContext);
    }

}
