package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.TaskContext;

public interface TaskExecutor {

        void executeTask(TaskContext taskContext) throws Exception;
}
