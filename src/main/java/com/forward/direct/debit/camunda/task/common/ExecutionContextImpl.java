package com.forward.direct.debit.camunda.task.common;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContextImpl implements ExecutionContext{

    private final DelegateExecution delegateExecution;
    private Map<String,Object> variablesMap = new HashMap<>();

    public ExecutionContextImpl(DelegateExecution delegateExecution) {
        this.delegateExecution = delegateExecution;
        this.variablesMap = delegateExecution.getVariables();
    }

    @Override
    public Object getVariable(String key) {
        return variablesMap.get(key);
    }

    @Override
    public void setVariable(String key, Object value) {
        variablesMap.put(key, value);
        delegateExecution.setVariable(key, value);
    }

    @Override
    public Map<String, Object> getVariables() {
        return variablesMap;
    }
}
