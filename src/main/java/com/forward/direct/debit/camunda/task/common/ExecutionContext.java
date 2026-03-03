package com.forward.direct.debit.camunda.task.common;

import java.util.Map;

public interface ExecutionContext {

    public Object getVariable(String key);
    public void setVariable(String key, Object value);

    public Map<String, Object> getVariables();
}
