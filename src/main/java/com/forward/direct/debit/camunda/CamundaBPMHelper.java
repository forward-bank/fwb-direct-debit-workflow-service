package com.forward.direct.debit.camunda;

import com.forward.direct.debit.camunda.task.executor.CamundaTaskExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class CamundaBPMHelper {

    private static Map<String,Object> taskExecutorInstanceMap = new ConcurrentHashMap<>();

    public static CamundaTaskExecutor getTaskExecutor(String taskActivityId) {
        String executorClassName = CamundaSetup.getInstance().getTaskMap().get(taskActivityId);
        try {
             if(!taskExecutorInstanceMap.containsKey(executorClassName)){
                 Class<?> taskExecutorClass = Class.forName(executorClassName);
                 CamundaTaskExecutor taskExecutor = (CamundaTaskExecutor) taskExecutorClass.newInstance();
                 taskExecutorInstanceMap.put(executorClassName, taskExecutorClass);
             }
            return (CamundaTaskExecutor) taskExecutorInstanceMap.get(executorClassName);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
