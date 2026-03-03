package com.forward.direct.debit.executor;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Callable task that starts a single Camunda process instance.
 * Returns the process instance ID as a String upon successful execution.
 * One instance is created and submitted to CamundaProcessExecutor per MQ message received.
 */
public class BusinessProcessExecutionThread implements Callable<String> {

    private final RuntimeService runtimeService;
    private final String processDefinitionKey;
    private final String businessKey;
    private final Map<String, Object> processVariables;

    public BusinessProcessExecutionThread(RuntimeService runtimeService,
                                          String processDefinitionKey,
                                          String businessKey,
                                          Map<String, Object> processVariables) {
        this.runtimeService       = runtimeService;
        this.processDefinitionKey = processDefinitionKey;
        this.businessKey          = businessKey;
        this.processVariables     = processVariables;
    }

    /**
     * Starts the Camunda process instance and returns the process instance ID.
     *
     * @return process instance ID on success
     * @throws BusinessProcessExecutionException on failure — wraps the root cause
     *         so the Future<String> surfaces a typed exception to the caller
     */
    @Override
    public String call() throws BusinessProcessExecutionException {
        String threadName = Thread.currentThread().getName();

        System.out.println("[" + threadName + "] BusinessProcessExecutionThread call() Starting Camunda business process...");
        System.out.println("  Business Key : " + businessKey);

        try {
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey,
                    businessKey,
                    processVariables
            );

            String processInstanceId = instance.getId();

            System.out.println("[" + threadName + "] ✓ Process instance started successfully");
            System.out.println("  Instance ID  : " + processInstanceId);

            return processInstanceId;  // returned via Future<String>.get()

        } catch (Exception e) {
            String errorMsg = String.format(
                    "[%s] ✗ Failed to start process instance | businessKey=%s | cause=%s",
                    threadName, businessKey, e.getMessage()
            );
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new BusinessProcessExecutionException(
                    "Process trigger failed for businessKey=" + businessKey, e
            );
        }
    }

    // -------------------------------------------------------------------------
    // Typed exception — callers can distinguish process failures from other errors
    // -------------------------------------------------------------------------

    public static class BusinessProcessExecutionException extends Exception {
        private final String businessKey;

        public BusinessProcessExecutionException(String message, Throwable cause) {
            super(message, cause);
            this.businessKey = extractBusinessKey(message);
        }

        public String getBusinessKey() {
            return businessKey;
        }

        private static String extractBusinessKey(String message) {
            // parse "businessKey=<value>" from the message for convenience
            if (message != null && message.contains("businessKey=")) {
                return message.substring(message.indexOf("businessKey=") + 12);
            }
            return "unknown";
        }
    }
}