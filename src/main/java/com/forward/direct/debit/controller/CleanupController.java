package com.forward.direct.debit.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CleanupController {

    @Autowired
    private RuntimeService runtimeService;

    @DeleteMapping("/dev/cleanup-instances")
    public String cleanup() {
        List<ProcessInstance> instances = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey("direct-debit-process")
                .list();

        instances.forEach(pi ->
                runtimeService.deleteProcessInstance(pi.getId(), "dev cleanup"));

        return "Deleted " + instances.size() + " instances";
    }
}

//-- Only do this in dev, never production
//DELETE FROM act_ru_execution;
//DELETE FROM act_ru_variable;
//DELETE FROM act_ru_task;
//DELETE FROM act_ru_identitylink;

// ERROR:  update or delete on table "act_ru_execution" violates foreign key constraint "act_fk_var_exe" on table "act_ru_variable"
//Key (id_)=(cfd2f7ab-1443-11f1-99ff-00155df5fbdf) is still referenced from table "act_ru_variable".
//
//SQL state: 23503
//Detail: Key (id_)=(cfd2f7ab-1443-11f1-99ff-00155df5fbdf) is still referenced from table "act_ru_variable".
