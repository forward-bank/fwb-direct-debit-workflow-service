package com.forward.direct.debit.camunda.task.common;

public interface ExecutionContext {

    public Object getVariable(String key);
    public void setVariable(String key, Object value);

}
