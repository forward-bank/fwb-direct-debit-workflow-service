package com.forward.direct.debit.camunda;

import com.forward.direct.debit.camunda.task.executor.MessageExecutor;
import com.forward.direct.debit.camunda.task.executor.TaskExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class CamundaBPMHelper {

    private static Map<String,Object> executorInstanceMap = new ConcurrentHashMap<>();

    public static TaskExecutor getTaskExecutor(String taskActivityId) {
        System.out.println("CamundaBPMHelper Retrieving Task Executor for Task Activity ID: " + taskActivityId);
        String executorClassName = CamundaSetup.getInstance().getTaskMap().get(taskActivityId);

        try {
             if(!executorInstanceMap.containsKey(executorClassName)){
                 Class<?> taskExecutorClass = Class.forName(executorClassName);
                 TaskExecutor taskExecutor = (TaskExecutor) taskExecutorClass.newInstance();
                 executorInstanceMap.put(executorClassName, taskExecutor);
             }

            return (TaskExecutor) executorInstanceMap.get(executorClassName);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MessageExecutor getMessageExecutor(String messageName) {
        System.out.println("CamundaBPMHelper Retrieving Message Executor for message name: " + messageName);
        String executorClassName = CamundaSetup.getInstance().getExecutorClassForMessageName(messageName);

        try {
            if(!executorInstanceMap.containsKey(executorClassName)){
                Class<?> messageExecutorClass = Class.forName(executorClassName);
                MessageExecutor messageExecutor = (MessageExecutor) messageExecutorClass.newInstance();
                executorInstanceMap.put(executorClassName, messageExecutor);
            }

            return (MessageExecutor) executorInstanceMap.get(executorClassName);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
