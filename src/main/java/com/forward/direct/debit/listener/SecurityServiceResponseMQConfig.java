package com.forward.direct.debit.listener;

public class SecurityServiceResponseMQConfig {

    private final String host;
    private final int    port;
    private final String channel;
    private final String queueManager;
    public static final String RESPONSE_QUEUE = "SECURITY.SERVICE.RESPONSE.QUEUE";

    public SecurityServiceResponseMQConfig(String host, int port, String channel, String queueManager) {
        this.host         = host;
        this.port         = port;
        this.channel      = channel;
        this.queueManager = queueManager;
    }

    /**
     * Reads from system properties (-D flags) with sensible defaults.
     * Usage: java -Dmq.host=myhost -Dmq.port=1414 -jar syntax-validation-service.jar
     */
    public static SecurityServiceResponseMQConfig fromSystemPropertiesOrDefaults() {
        return new SecurityServiceResponseMQConfig(
                System.getProperty("mq.host",         "localhost"),
                Integer.parseInt(System.getProperty("mq.port",    "1414")),
                System.getProperty("mq.channel",      "SYSTEM.DEF.SVRCONN"),
                System.getProperty("mq.queueManager", "MY.TEST.QMNGR")
        );
    }

    public String getHost()         { return host; }
    public int    getPort()         { return port; }
    public String getChannel()      { return channel; }
    public String getQueueManager() { return queueManager; }
}