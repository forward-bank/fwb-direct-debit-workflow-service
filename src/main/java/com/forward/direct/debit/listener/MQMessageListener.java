package com.forward.direct.debit.listener;

import com.forward.direct.debit.executor.CamundaProcessExecutor;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Asynchronous message listener for IBM MQ
 * Listens and consumes messages as they arrive in the queue
 */

public class MQMessageListener implements MessageListener {

    private final String queueName;
    private final AtomicInteger messageCount;
    private final DateTimeFormatter formatter;
    private MessageConsumer consumer;

    // Use constructor injection because this class is instantiated with `new` in configuration
    private final CamundaProcessExecutor camundaProcessExecutor;
    private final String processDefinitionKey;

    public MQMessageListener(String queueName, CamundaProcessExecutor camundaProcessExecutor, String processDefinitionKey) {
        this.queueName = queueName;
        this.messageCount = new AtomicInteger(0);
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        this.camundaProcessExecutor = camundaProcessExecutor;
        this.processDefinitionKey = processDefinitionKey;
    }

    /**
     * Initialize the message listener
     */
    public void initialize(MQConnectionManager connectionManager) throws JMSException {
        System.out.println("=========================================");
        System.out.println("Initializing Message Listener");
        System.out.println("=========================================");
        System.out.println("Queue: " + queueName);
        System.out.println("=========================================" + "\n");
        // Get the queue
        Queue queue = connectionManager.getSession().createQueue(queueName);
        // Create message consumer
        consumer = connectionManager.getSession().createConsumer(queue);
        // Set this as the message listener
        consumer.setMessageListener(this);
        System.out.println("✓ Message Listener initialized successfully");
        System.out.println("✓ Now listening for messages on queue: " + queueName);
        System.out.println("\n=========================================");
        System.out.println("WAITING FOR MESSAGES...");
        System.out.println("=========================================");
        System.out.println("Press Ctrl+C to stop listening\n");
    }

    /**
     * Called automatically when a message arrives
     */
    @Override
    public void onMessage(Message message) {
        System.out.println("<<<<<< MQMessageListener received a message >>>>>>>>");
        int currentCount = messageCount.incrementAndGet();
        String timestamp = LocalDateTime.now().format(formatter);

        try {
            triggerBusinessProcess(message);
        } catch (JMSException e) {
            System.err.println("JMS error: " + e.getMessage());
        } catch (Exception e) {  // <-- prevent exceptions from propagating and causing redelivery loops
            System.err.println("Unexpected error processing message: " + e.getMessage());
            e.printStackTrace();
            // With AUTO_ACKNOWLEDGE the message is already acknowledged; do not rethrow unless you want redelivery
        }
    }

    private void triggerBusinessProcess(Message message) throws JMSException {
        String businessKey = message.getJMSMessageID();
        Map<String, Object> vars = extractProcessVariables(message);

        // camundaProcessExecutor is now injected via constructor; guard just in case
        if (camundaProcessExecutor == null) {
            throw new IllegalStateException("CamundaProcessExecutor is not initialized");
        }

        Future<String> future = camundaProcessExecutor.triggerProcess(
                processDefinitionKey,
                businessKey,
                vars
        );

        // System.out.println("│ ✓ BusinessProcessExecutionThread submitted (businessKey=" + businessKey + ")");
    }

    /**
     * Extract all JMS message data into a flat map before handing off to worker thread.
     * IMPORTANT: JMS Message objects must be read on the listener thread, not the worker thread.
     */
    private Map<String, Object> extractProcessVariables(Message message) throws JMSException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("jmsMessageId",    message.getJMSMessageID());
        vars.put("jmsCorrelationId", message.getJMSCorrelationID());
        vars.put("jmsPriority",     message.getJMSPriority());
        vars.put("jmsTimestamp",    message.getJMSTimestamp());
        vars.put("sourceQueue",     queueName);

        if (message instanceof TextMessage) {
            // vars.put("messageType",    "TEXT");
            vars.put("incomingMessage", message.getBody(String.class));
        }
        return vars;
    }

    /**
     * Close the message consumer
     */
    public void close() {
        try {
            if (consumer != null) {
                consumer.close();
                System.out.println("✓ Message Listener closed");
            }
        } catch (JMSException e) {
            System.err.println("✗ Error closing message consumer: " + e.getMessage());
        }
    }

    /**
     * Get the total number of messages received
     */
    public int getMessageCount() {
        return messageCount.get();
    }
}

