package com.forward.direct.debit.camunda.task.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.integrations.fileprocess.FileProcessServiceClient;
import com.forward.direct.debit.integrations.fileprocess.model.DispatchResponse;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class OrchestrationWorkflowDispatchTaskDefinition extends ServiceTaskDefinition{

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final FileProcessServiceClient fileProcessServiceClient;

    public OrchestrationWorkflowDispatchTaskDefinition(ExecutionContext executionContext,
                                                       ApplicationContext applicationContext) {
        super(executionContext,applicationContext);
        fileProcessServiceClient = applicationContext.getBean(FileProcessServiceClient.class);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("OrchestrationWorkflowDispatchTaskDefinition Executing Orchestration Workflow Dispatch Request Task...");
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
        executionContext.setVariable("correlationId", correlationId);
        dispatchOrchestrationWorkflow(1l,2l);
        System.out.println("Orchestration workflow dispatched successfully.");
        System.out.println("=".repeat(80));
    }

    private DispatchResponse dispatchOrchestrationWorkflow(Long customerId, Long fileId) {
        System.out.println("Dispatching orchestration workflow...");
        return fileProcessServiceClient.dispatch(customerId, fileId);
    }
}
