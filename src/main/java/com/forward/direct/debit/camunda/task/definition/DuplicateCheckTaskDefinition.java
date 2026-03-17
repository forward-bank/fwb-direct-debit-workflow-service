package com.forward.direct.debit.camunda.task.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.*;
import java.util.Map;

public class DuplicateCheckTaskDefinition extends ServiceTaskDefinition{

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public DuplicateCheckTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("DuplicateCheckTaskDefinition Executing Duplicate Check Service Task...");
        // implement a method to print all the variables in executionContext for debugging
        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });

        System.out.println("=".repeat(80));
        setVariable("is_file_duplicate", false);
    }

}
