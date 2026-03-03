package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;

public interface TaskExecutor {

        void executeTask(ExecutionContext executionContext) throws Exception;
}
