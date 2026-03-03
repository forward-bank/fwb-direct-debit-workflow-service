package com.forward.direct.debit.camunda.task.common;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContextImpl implements ExecutionContext{

    private Map<String,Object> variablesMap = new HashMap<>();

    public ExecutionContextImpl(Map<String,Object> variablesMap) {
        this.variablesMap = variablesMap;
    }

    @Override
    public Object getVariable(String key) {
        return variablesMap.get(key);
    }

    @Override
    public void setVariable(String key, Object value) {
        variablesMap.put(key, value);
    }

    @Override
    public Map<String, Object> getVariables() {
        return variablesMap;
    }
}
