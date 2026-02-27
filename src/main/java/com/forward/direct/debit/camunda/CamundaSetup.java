package com.forward.direct.debit.camunda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CamundaSetup {

    private static final Map<String, String> taskMap = new HashMap<>();
    private static final CamundaSetup INSTANCE = createInstance();

    public  CamundaSetup() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getResourceAsStream("/tasks.json");
        JsonNode root = mapper.readTree(inputStream);

        for (JsonNode taskNode : root.get("tasks")) {
            String taskName      = taskNode.get("taskName").asText();
            String executorClass = taskNode.get("executorClass").asText();
            taskMap.put(taskName, executorClass);
        }
    }

    public static CamundaSetup getInstance() {
        return INSTANCE;
    }
    public Map<String, String> getTaskMap() {
        return taskMap;
    }

    private static CamundaSetup createInstance() {
        try {
            return new CamundaSetup();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to load tasks.json: " + e.getMessage());
        }
    }

}