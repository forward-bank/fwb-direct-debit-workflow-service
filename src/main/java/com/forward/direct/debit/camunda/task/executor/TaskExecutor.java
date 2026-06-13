package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import org.springframework.context.ApplicationContext;

public interface TaskExecutor {

        void executeTask(ExecutionContext executionContext,
                         ApplicationContext applicationContext) throws Exception;
}
