package com.forward.direct.debit.camunda.task.definition;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import org.springframework.context.ApplicationContext;

public abstract class ServiceTaskDefinition {

    protected ExecutionContext executionContext;
    protected ApplicationContext applicationContext;

    // TODO : Pass ApplicationContext to ServiceTaskDefinition and let it fetch the required beans
    //  instead of passing the required beans from ServiceTaskExecutor to ServiceTaskDefinition
    protected ServiceTaskDefinition(ExecutionContext executionContext, ApplicationContext applicationContext) {
        this.executionContext = executionContext;
        this.applicationContext = applicationContext;
    }

    public abstract void execute() throws Exception;

    public Object getVariable(String key) {
        return executionContext.getVariable(key);
    }
    public void setVariable(String key, Object value) {
        executionContext.setVariable(key, value);
    }
}
