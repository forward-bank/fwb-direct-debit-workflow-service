package com.forward.direct.debit.camunda.task.common;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContextImpl implements ExecutionContext{

    private Map<String,Object> variableMap = new HashMap<>();

    @Override
    public Object getVariable(String key) {
        return variableMap.get(key);
    }

    @Override
    public void setVariable(String key, Object value) {
        variableMap.put(key, value);
    }
}
