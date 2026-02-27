package com.forward.direct.debit.camunda.task.definition;

import com.forward.direct.debit.camunda.task.common.TaskContext;

public abstract class ServiceTaskDefinition {

    public abstract void execute(TaskContext context) throws Exception;

}
