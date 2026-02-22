package com.forward.direct.debit;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableProcessApplication
public class DirectDebitWorkflowApplication implements CommandLineRunner {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(DirectDebitWorkflowApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Start the process using the Process Definition Key from your BPMN file
        // runtimeService.startProcessInstanceByKey("direct-debit-process");
        System.out.println("Direct Debit Workflow Service started!");
    }
}