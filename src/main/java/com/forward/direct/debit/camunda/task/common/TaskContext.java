//package com.forward.direct.debit.camunda.task.common;
//
//import org.camunda.bpm.engine.delegate.DelegateExecution;
//
//public class TaskContext implements ExecutionContext{
//
//    private final DelegateExecution delegateExecution;
//
//    public TaskContext(DelegateExecution delegateExecution) {
//        this.delegateExecution = delegateExecution;
//    }
//
//    public String getTaskId() {
//        return delegateExecution.getCurrentActivityId();
//    }
//
//    public String getProcessInstanceId() {
//        return delegateExecution.getProcessInstanceId();
//    }
//
//    public String getProcessBusinessKey() {
//        return delegateExecution.getProcessBusinessKey();
//    }
//
//    public Object getVariable(String name) {
//        return delegateExecution.getVariable(name);
//    }
//
//    public void setVariable(String name, Object value) {
//        delegateExecution.setVariable(name, value);
//    }
//}