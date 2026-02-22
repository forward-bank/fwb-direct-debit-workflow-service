package com.forward.direct.debit.executor;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

@Component
public class CamundaProcessExecutor extends ThreadPoolTaskExecutor {

    private final RuntimeService runtimeService;

    @Value("${camunda.executor.core.pool.size:5}")
    private int corePoolSize;

    @Value("${camunda.executor.max.pool.size:20}")
    private int maxPoolSize;

    @Value("${camunda.executor.queue.capacity:100}")
    private int queueCapacity;

    @Value("${camunda.executor.keep.alive.seconds:60}")
    private int keepAliveSeconds;

    @Value("${camunda.executor.thread.name.prefix:camunda-process-worker-}")
    private String threadNamePrefix;

    public CamundaProcessExecutor(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostConstruct
    public void configure() {
        setCorePoolSize(corePoolSize);
        setMaxPoolSize(maxPoolSize);
        setQueueCapacity(queueCapacity);
        setKeepAliveSeconds(keepAliveSeconds);
        setThreadNamePrefix(threadNamePrefix);
        setWaitForTasksToCompleteOnShutdown(true);
        setAwaitTerminationSeconds(30);
        setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        initialize();
    }

    /**
     * Submits a BusinessProcessExecutionThread to the pool.
     * Returns Future<String> — callers can block on .get() to retrieve
     * the Camunda process instance ID, or discard it for fire-and-forget.
     *
     * @return Future<String> containing the process instance ID on success
     */
    public Future<String> triggerProcess(String processDefinitionKey,
                                         String businessKey,
                                         Map<String, Object> processVariables) {
        try {
            BusinessProcessExecutionThread task = new BusinessProcessExecutionThread(
                    runtimeService,
                    processDefinitionKey,
                    businessKey,
                    processVariables
            );
            return submit(task);  // submit(Callable<T>) inherited from ThreadPoolTaskExecutor

        } catch (RejectedExecutionException e) {
            System.err.println("[CamundaProcessExecutor] ✗ Task REJECTED — pool and queue are full.");
            System.err.println("  Business Key : " + businessKey);
            System.err.println("  Process Key  : " + processDefinitionKey);
            throw e;
        }
    }

    public void logPoolStats() {
        System.out.printf(
                "[CamundaProcessExecutor] Pool Stats → Active: %d | Pool Size: %d | Queue: %d | Completed: %d%n",
                getActiveCount(),
                getPoolSize(),
                getThreadPoolExecutor().getQueue().size(),
                getThreadPoolExecutor().getCompletedTaskCount()
        );
    }
}