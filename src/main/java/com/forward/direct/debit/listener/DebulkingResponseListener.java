package com.forward.direct.debit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.CamundaBPMHelper;
import com.forward.direct.debit.camunda.CamundaSetup;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.MessageExecutionContextImpl;
import com.forward.direct.debit.camunda.task.executor.MessageExecutor;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DebulkingResponseListener implements MessageListener {

    private static final ObjectMapper OBJECT_MAPPER   = new ObjectMapper();
    private static final String BPMN_MESSAGE_NAME     = "debulking_response_message";
    private static final int    MAX_RETRY_ATTEMPTS    = 10;
    private static final long   RETRY_DELAY_MS        = 500;

    private final DebulkingMQConfig       mqConfig;
    private final RuntimeService runtimeService;
    private final ScheduledExecutorService retryScheduler =
            Executors.newScheduledThreadPool(2);

    private Connection    connection;
    private Session       session;
    private MessageConsumer consumer;

    public DebulkingResponseListener(DebulkingMQConfig mqConfig, RuntimeService runtimeService) {
        this.mqConfig       = mqConfig;
        this.runtimeService = runtimeService;
    }

    public void start() {
        try {
            MQConnectionFactory factory = createFactory();
            connection = factory.createConnection();
            session    = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(DebulkingMQConfig.RESPONSE_QUEUE);
            consumer   = session.createConsumer(queue);
            consumer.setMessageListener(this);
            connection.start();
            System.out.println("✓ DebulkingResponseListener started");
            System.out.println("✓ Listening on: " + DebulkingMQConfig.RESPONSE_QUEUE);
        } catch (JMSException e) {
            throw new RuntimeException("Failed to start DebulkingResponseListener", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("=".repeat(80));
        System.out.println("DebulkingResponseListener: message received");

        try {
            if (!(message instanceof TextMessage)) {
                System.err.println("✗ Unsupported message type: " + message.getClass().getSimpleName());
                return;
            }

            // Extract everything from the JMS message HERE on the listener thread
            // before handing off — JMS message must not be read on another thread
            String correlationId = message.getJMSCorrelationID();
            String queueName     = DebulkingMQConfig.RESPONSE_QUEUE;

            String messageName       = CamundaSetup.getInstance().getQueueMessageNameMap().get(queueName);
            MessageExecutor executor = CamundaBPMHelper.getMessageExecutor(messageName);

            Map<String, Object> seed = new HashMap<>();
            seed.put("correlationId", correlationId);
            ExecutionContext executionContext = new MessageExecutionContextImpl(seed, BPMN_MESSAGE_NAME);

            // Task definition reads body and sets variables on executionContext
            executor.executeMessage(executionContext, message);

            // Schedule first correlation attempt immediately (delay=0),
            // retries will back off by RETRY_DELAY_MS — listener thread is free instantly
            scheduleCorrelationAttempt(messageName, correlationId,
                    executionContext.getVariables(), 1, 0);

        } catch (Throwable t) {
            System.err.println("!!! FAILURE in DebulkingResponseListener: " + t.getMessage());
            t.printStackTrace();
        } finally {
            System.out.println("=".repeat(80));
        }
    }

    private void scheduleCorrelationAttempt(String messageName, String correlationId,
                                            Map<String, Object> vars, int attempt, long delayMs) {
        retryScheduler.schedule(() -> {
            try {
                runtimeService.createMessageCorrelation(messageName)
                        .processInstanceVariableEquals("correlationId", correlationId)
                        .setVariables(vars)
                        .correlate();

                System.out.println("  ✓ Camunda process instance resumed"
                        + (attempt > 1 ? " (attempt " + attempt + ")" : ""));

            } catch (MismatchingMessageCorrelationException e) {
                System.out.println("  ⚠ Correlation attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS
                        + " failed for correlationId=" + correlationId
                        + " — receive task not yet committed, retrying in " + RETRY_DELAY_MS + "ms...");

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    scheduleCorrelationAttempt(messageName, correlationId,
                            vars, attempt + 1, RETRY_DELAY_MS);
                } else {
                    System.err.println("  ✗ Correlation gave up after " + MAX_RETRY_ATTEMPTS
                            + " attempts for correlationId=" + correlationId);
                    // TODO: dead-letter / alert / persist for manual replay
                }

            } catch (Throwable t) {
                System.err.println("  ✗ Unexpected error during correlation attempt " + attempt
                        + " for correlationId=" + correlationId + ": " + t.getMessage());
                t.printStackTrace();
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        retryScheduler.shutdown();
        try {
            if (!retryScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("WARN: retryScheduler did not terminate cleanly");
                retryScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            retryScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        closeQuietly(consumer,   "consumer");
        closeQuietly(session,    "session");
        closeQuietly(connection, "connection");
        System.out.println("✓ DebulkingResponseListener stopped");
    }

    private MQConnectionFactory createFactory() throws JMSException {
        MQConnectionFactory factory = new MQConnectionFactory();
        factory.setHostName(mqConfig.getHost());
        factory.setPort(mqConfig.getPort());
        factory.setChannel(mqConfig.getChannel());
        factory.setQueueManager(mqConfig.getQueueManager());
        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        return factory;
    }

    private void closeQuietly(MessageConsumer c, String name) {
        if (c != null) try { c.close(); } catch (JMSException e) { warn(name, e); }
    }

    private void closeQuietly(Session s, String name) {
        if (s != null) try { s.close(); } catch (JMSException e) { warn(name, e); }
    }

    private void closeQuietly(Connection c, String name) {
        if (c != null) try { c.close(); } catch (JMSException e) { warn(name, e); }
    }

    private void warn(String resource, JMSException e) {
        System.err.println("WARN: Failed to close " + resource + ": " + e.getMessage());
    }
}