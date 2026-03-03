package com.forward.direct.debit.camunda.task.definition;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;

public abstract class ServiceTaskDefinition {

    protected ExecutionContext executionContext;

    protected ServiceTaskDefinition(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public abstract void execute() throws Exception;

    public Object getVariable(String key) {
        return executionContext.getVariable(key);
    }
    public void setVariable(String key, Object value) {
        executionContext.setVariable(key, value);
    }
}
