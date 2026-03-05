package com.forward.direct.debit.camunda.task.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.*;
import java.util.Map;

public class SyntaxValidationRequestTaskDefinition extends ServiceTaskDefinition{

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SyntaxValidationRequestTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("SyntaxValidationRequestTaskDefinition Executing Syntax Validation Request Task...");
        // implement a method to print all the variables in executionContext for debugging
        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });

        String paymentXmlPath = "FWB_DIRECT_DEBIT/PAYMENT_FILES/2026/02/04/INCOMING/I1234567890123.FWB.pain00800108.ABCD123.PM.pgp_12345.145.xml";
        String payload = OBJECT_MAPPER.writeValueAsString(
                Map.of("paymentXmlPath", paymentXmlPath)
        );
        String correlationId = (String) executionContext.getVariable("jmsMessageId");
        sendToQueue(payload, correlationId);
        System.out.println("=".repeat(80));
    }

    // -------------------------------------------------------------------------
    // Creates a dedicated short-lived connection just for this send.
    // Completely independent from the inbound MQConnectionManager — no
    // shared session, no threading concerns.
    // -------------------------------------------------------------------------
    private void sendToQueue(String payload, String correlationId) throws JMSException {
        MQConnectionFactory factory = new MQConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(1414);
        factory.setChannel("SYSTEM.DEF.SVRCONN");
        factory.setQueueManager("MY.TEST.QMNGR");
        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

        final String syntaxValidationQueueName = "SYNTAX.VALIDATION.REQUEST.QUEUE";
        Connection connection = null;
        Session session       = null;
        MessageProducer producer = null;
        try {
            connection = factory.createConnection();
            connection.start();
            session  = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(syntaxValidationQueueName);
            producer = session.createProducer(queue);
            TextMessage message = session.createTextMessage(payload);
            message.setJMSCorrelationID(correlationId);
            producer.send(message);
            System.out.println("✓ Message sent to " + syntaxValidationQueueName);
            System.out.println("  Payload        : " + payload);
            System.out.println("  Correlation ID : " + correlationId);
        } finally {
            if (producer   != null) try { producer.close();   } catch (JMSException e) { System.err.println("WARN: " + e.getMessage()); }
            if (session    != null) try { session.close();    } catch (JMSException e) { System.err.println("WARN: " + e.getMessage()); }
            if (connection != null) try { connection.close(); } catch (JMSException e) { System.err.println("WARN: " + e.getMessage()); }
        }
    }
}
