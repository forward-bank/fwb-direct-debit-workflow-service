package com.forward.direct.debit.camunda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CamundaSetup {

    private static final Map<String, String> taskMap = new ConcurrentHashMap<>();
    private static final Map<String, String> queueMessageNameMap = new ConcurrentHashMap<>();
    private static final Map<String, String> messageNameExecutorMap = new ConcurrentHashMap<>();

    private static final CamundaSetup INSTANCE = createInstance();

    public  CamundaSetup() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getResourceAsStream("/tasks.json");
        JsonNode root = mapper.readTree(inputStream);

        for (JsonNode taskNode : root.get("ServiceTasks")) {
            String taskName      = taskNode.get("taskName").asText();
            String executorClass = taskNode.get("executorClass").asText();
            taskMap.put(taskName, executorClass);
        }

        for (JsonNode taskNode : root.get("ReceiveTasks")) {
            String queueName      = taskNode.get("queueName").asText();
            String messageName = taskNode.get("messageName").asText();
            String executorClass = taskNode.get("executorClass").asText();
            queueMessageNameMap.put(queueName, messageName);
            messageNameExecutorMap.put(messageName, executorClass);
        }
    }

    public static CamundaSetup getInstance() {
        return INSTANCE;
    }
    public Map<String, String> getTaskMap() {
        return taskMap;
    }
    public Map<String, String> getQueueMessageNameMap() {
        return queueMessageNameMap;
    }
    public Map<String, String> getMessageNameExecutorMap() {
        return messageNameExecutorMap;
    }

    private static CamundaSetup createInstance() {
        try {
            return new CamundaSetup();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to load tasks.json: " + e.getMessage());
        }
    }

    public String getExecutorClassForMessageName(String messageName) {
        return messageNameExecutorMap.get(messageName);
    }

}