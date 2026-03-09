package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.task.common.ExecutionContext;

import javax.jms.Message;

public interface MessageExecutor {

        void executeMessage(ExecutionContext executionContext, Message message) throws Exception;
}
