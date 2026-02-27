package com.forward.direct.debit.camunda.task.common;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

import java.util.Map;

public class TaskContext {

    private final ActivityExecution execution;

    public TaskContext(ActivityExecution execution) {
        this.execution = execution;
    }

    public String getTaskName() {
        return execution.getActivity().getId();
    }

    public String getProcessInstanceId() {
        return execution.getProcessInstanceId();
    }

    public Object getVariable(String name) {
        return execution.getVariable(name);
    }

    public void setVariable(String name, Object value) {
        execution.setVariable(name, value);
    }

    public Map<String, Object> getVariables() {
        return execution.getVariables();
    }
}