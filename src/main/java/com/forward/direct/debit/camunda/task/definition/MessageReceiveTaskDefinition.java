package com.forward.direct.debit.camunda.task.definition;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;

import javax.jms.Message;

public abstract class MessageReceiveTaskDefinition {

    protected ExecutionContext executionContext;

    protected MessageReceiveTaskDefinition(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public abstract void execute(Message message) throws Exception;

    public Object getVariable(String key) {
        return executionContext.getVariable(key);
    }
    public void setVariable(String key, Object value) {
        executionContext.setVariable(key, value);
    }
}
