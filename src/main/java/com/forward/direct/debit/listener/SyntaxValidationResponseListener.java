package com.forward.direct.debit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import javax.jms.*;
import org.camunda.bpm.engine.RuntimeService;

import javax.jms.Connection;
import javax.jms.Session;
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
            System.out.println("  Raw correlationId from MQ : [" + correlationId + "]");

// Check what Camunda actually has stored
            runtimeService.createProcessInstanceQuery()
                    .variableValueEquals("correlationId", correlationId)
                    .list()
                    .forEach(pi -> System.out.println("  Matching process instance: " + pi.getId()));

// Also list ALL active instances to see what's waiting
            runtimeService.createProcessInstanceQuery()
                    .list()
                    .forEach(pi -> System.out.println("  Active instance: " + pi.getId()
                            + " businessKey: " + pi.getBusinessKey()));
            String body          = ((TextMessage) message).getText();

            System.out.println("  JMSCorrelationID : " + correlationId);
            System.out.println("  Payload          : " + body);

            Map<String, Object> responseMap = OBJECT_MAPPER.readValue(body, Map.class);
            String status    = (String) responseMap.get("status");
            String errorCode = (String) responseMap.get("errorCode");

            System.out.println("  Status    : " + status);
            System.out.println("  ErrorCode : " + errorCode);

            // Correlate back to the waiting Camunda receive task
            runtimeService.createMessageCorrelation(BPMN_MESSAGE_NAME)
                    .processInstanceVariableEquals("correlationId", correlationId)
                    .setVariable("syntaxValidationStatus",    status)
                    .setVariable("syntaxValidationErrorCode", errorCode)
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