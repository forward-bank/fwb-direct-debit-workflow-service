package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.CustomerValidationTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.ServiceTaskDefinition;
import com.forward.direct.debit.camunda.task.definition.SyntaxValidationRequestTaskDefinition;
import org.springframework.context.ApplicationContext;

public class SyntaxValidationRequestTaskExecutor extends ServiceTaskExecutor {

    @Override
    ServiceTaskDefinition getTaskDefinition(ExecutionContext executionContext, ApplicationContext applicationContext) {
        return new SyntaxValidationRequestTaskDefinition(executionContext, applicationContext);
    }

}
