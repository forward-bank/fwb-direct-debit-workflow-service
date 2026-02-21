package com.forward.direct.debit.config;

import com.forward.direct.debit.listener.MQConnectionManager;
import com.forward.direct.debit.listener.MQMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.JMSException;

/**
 * Spring Boot Configuration for IBM MQ Message Listener
 * Using @Bean with initMethod and destroyMethod for lifecycle management
 *
 * This is the cleanest approach - Spring manages the lifecycle automatically
 */
@Configuration
public class MQListenerConfiguration {

    @Value("${ibm.mq.host:localhost}")
    private String host;

    @Value("${ibm.mq.port:1414}")
    private int port;

    @Value("${ibm.mq.channel:SYSTEM.DEF.SVRCONN}")
    private String channel;

    @Value("${ibm.mq.queueManager:MY.TEST.QMNGR}")
    private String queueManager;

    @Value("${ibm.mq.queue:FIRST.TEST.QUEUE}")
    private String queueName;

    /**
     * Create and configure the MQ Listener Service bean
     * Spring will automatically call init() and shutdown() methods
     */
    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public MQListenerService mqListenerService() {
        return new MQListenerService(host, port, channel, queueManager, queueName);
    }

    /**
     * Service class that manages the IBM MQ Listener lifecycle
     */
    public static class MQListenerService {
        private final String host;
        private final int port;
        private final String channel;
        private final String queueManager;
        private final String queueName;

        private MQConnectionManager connectionManager;
        private MQMessageListener messageListener;

        public MQListenerService(String host, int port, String channel,
                                 String queueManager, String queueName) {
            this.host = host;
            this.port = port;
            this.channel = channel;
            this.queueManager = queueManager;
            this.queueName = queueName;
        }

        /**
         * Initialization method called by Spring after bean creation
         */
        public void init() {
            try {
                System.out.println("\n╔═════════════════════════════════════════╗");
                System.out.println("║   INITIALIZING IBM MQ LISTENER          ║");
                System.out.println("╚═════════════════════════════════════════╝");
                System.out.println("Host: " + host);
                System.out.println("Port: " + port);
                System.out.println("Queue Manager: " + queueManager);
                System.out.println("Queue: " + queueName);
                System.out.println();

                // Create connection manager
                connectionManager = new MQConnectionManager(host, port, channel, queueManager);

                // Connect to IBM MQ
                connectionManager.connect();

                // Create and initialize message listener
                messageListener = new MQMessageListener(queueName);
                messageListener.initialize(connectionManager);

                System.out.println("✓ IBM MQ Listener started successfully\n");

            } catch (JMSException e) {
                System.err.println("✗ Failed to start IBM MQ Listener");
                throw new RuntimeException("Failed to initialize MQ Listener", e);
            }
        }

        /**
         * Cleanup method called by Spring during shutdown
         */
        public void shutdown() {
            System.out.println("\n╔═════════════════════════════════════════╗");
            System.out.println("║        SHUTTING DOWN MQ LISTENER        ║");
            System.out.println("╚═════════════════════════════════════════╝");

            if (messageListener != null) {
                System.out.println("Total messages processed: " + messageListener.getMessageCount());
                messageListener.close();
            }

            if (connectionManager != null) {
                connectionManager.disconnect();
            }

            System.out.println("✓ MQ Listener shutdown complete\n");
        }

        // Getters for monitoring
        public MQConnectionManager getConnectionManager() {
            return connectionManager;
        }

        public MQMessageListener getMessageListener() {
            return messageListener;
        }

        public int getMessageCount() {
            return messageListener != null ? messageListener.getMessageCount() : 0;
        }

        public boolean isConnected() {
            return connectionManager != null && connectionManager.getConnection() != null;
        }
    }
}