package com.forward.direct.debit.camunda.task.common;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

import java.util.Map;

public class TaskContext implements ExecutionContext{

    private  DelegateExecution delegateExecution;
    private ExecutionContext executionContext;

    public TaskContext(DelegateExecution delegateExecution, ExecutionContext executionContext) {
        this.delegateExecution = delegateExecution;
        this.executionContext = executionContext;
    }

    public String getTaskId() {
        return delegateExecution.getCurrentActivityId();
    }

    public String getProcessInstanceId() {
        return delegateExecution.getProcessInstanceId();
    }

    public String getProcessBusinessKey() {
        return delegateExecution.getProcessBusinessKey();
    }

    public Object getVariable(String name) {
        return executionContext.getVariable(name);
    }

    public void setVariable(String name, Object value) {
        executionContext.setVariable(name, value);
    }

//    public Map<String, Object> getVariables() {
//        return executionContext.getVariables();
//    }
}