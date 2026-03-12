package com.forward.direct.debit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.CamundaBPMHelper;
import com.forward.direct.debit.camunda.CamundaSetup;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.ExecutionContextImpl;
import com.forward.direct.debit.camunda.task.common.MessageExecutionContextImpl;
import com.forward.direct.debit.camunda.task.executor.MessageExecutor;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import javax.jms.*;
import org.camunda.bpm.engine.RuntimeService;

import javax.jms.Connection;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

public class SyntaxValidationResponseListener implements MessageListener {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BPMN_MESSAGE_NAME = "syntax_validation_response_message";
    private final MQConfig       mqConfig;
    private final RuntimeService runtimeService;

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    public SyntaxValidationResponseListener(MQConfig mqConfig, RuntimeService runtimeService) {
        this.mqConfig       = mqConfig;
        this.runtimeService = runtimeService;
    }

    public void start() {
        try {
            MQConnectionFactory factory = createFactory();
            connection = factory.createConnection();
            session    = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(MQConfig.RESPONSE_QUEUE);
            consumer   = session.createConsumer(queue);
            consumer.setMessageListener(this);

            connection.start();
            System.out.println("✓ SyntaxValidationResponseListener started");
            System.out.println("✓ Listening on: " + MQConfig.RESPONSE_QUEUE);
        } catch (JMSException e) {
            throw new RuntimeException("Failed to start SyntaxValidationResponseListener", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("=".repeat(80));
        System.out.println("SyntaxValidationResponseListener: message received");

        try {
            if (!(message instanceof TextMessage)) {
                System.err.println("✗ Unsupported message type: " + message.getClass().getSimpleName());
                return;
            }
            String correlationId = message.getJMSCorrelationID();
            String body          = ((TextMessage) message).getText();
            String queueName     = MQConfig.RESPONSE_QUEUE;

            // Resolve executor via CamundaBPMHelper (same pattern as service tasks)
            String messageName     = CamundaSetup.getInstance().getQueueMessageNameMap().get(queueName);
            MessageExecutor executor = CamundaBPMHelper.getMessageExecutor(messageName);

            // Build an ExecutionContext to carry variables into the task definition
            // Extract everything from JMS message HERE on the listener thread
            Map<String, Object> seed = new HashMap<>();
            seed.put("correlationId", correlationId);
            ExecutionContext executionContext = new MessageExecutionContextImpl(seed, BPMN_MESSAGE_NAME);
            executionContext.setVariable("correlationId", correlationId);

            // Pass the raw JMS message — the task definition reads it directly
            executor.executeMessage(executionContext, message);

            // After the task definition has run, correlate back to Camunda
            Map<String, Object> vars = executionContext.getVariables();

            runtimeService.createMessageCorrelation(messageName)
                    .processInstanceVariableEquals("correlationId", correlationId)
                    .setVariables(vars)
                    .correlate();

            System.out.println("  ✓ Camunda process instance resumed");

        } catch (Throwable t) {
            System.err.println("!!! FAILURE in SyntaxValidationResponseListener: " + t.getMessage());
            t.printStackTrace();
        } finally {
            System.out.println("=".repeat(80));
        }
    }

    public void stop() {
        closeQuietly(consumer,   "consumer");
        closeQuietly(session,    "session");
        closeQuietly(connection, "connection");
        System.out.println("✓ SyntaxValidationResponseListener stopped");
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