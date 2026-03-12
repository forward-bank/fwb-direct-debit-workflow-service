package com.forward.direct.debit.camunda.task.common;

import java.util.HashMap;
import java.util.Map;

public class MessageExecutionContextImpl implements ExecutionContext {

    private Map<String,Object> variablesMap = new HashMap<>();
    private String messageName;
    private String businessKey;
    private String processInstanceId;

    public MessageExecutionContextImpl(Map<String,Object> variablesMap,
           String messageName) {
        this.variablesMap = variablesMap;
        this.messageName = messageName;
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
