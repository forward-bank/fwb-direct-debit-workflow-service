package com.forward.direct.debit.camunda;

import com.forward.direct.debit.camunda.task.executor.TaskExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class CamundaBPMHelper {

    private static Map<String,Object> taskExecutorInstanceMap = new ConcurrentHashMap<>();

    public static TaskExecutor getTaskExecutor(String taskActivityId) {
        System.out.println("Retrieving Task Executor for Task Activity ID: " + taskActivityId);
        String executorClassName = CamundaSetup.getInstance().getTaskMap().get(taskActivityId);
        System.out.println("Mapped Executor Class Name: " + executorClassName);
        try {
             if(!taskExecutorInstanceMap.containsKey(executorClassName)){
                 Class<?> taskExecutorClass = Class.forName(executorClassName);
                 TaskExecutor taskExecutor = (TaskExecutor) taskExecutorClass.newInstance();
                 taskExecutorInstanceMap.put(executorClassName, taskExecutor);
             }

            return (TaskExecutor) taskExecutorInstanceMap.get(executorClassName);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
