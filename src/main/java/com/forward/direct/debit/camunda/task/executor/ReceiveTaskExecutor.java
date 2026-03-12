package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.definition.MessageReceiveTaskDefinition;

import javax.jms.Message;

public abstract class ReceiveTaskExecutor implements MessageExecutor{

    @Override
    public void executeMessage(ExecutionContext executionContext, Message message) throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("ReceiveTaskExecutor executeMessage()...");
        getTaskDefinition(executionContext).execute(message);
        System.out.println("=".repeat(80));
    }

    abstract MessageReceiveTaskDefinition getTaskDefinition(ExecutionContext executionContext);

}
