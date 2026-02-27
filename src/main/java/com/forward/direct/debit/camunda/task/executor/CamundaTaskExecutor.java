package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.CamundaBPMHelper;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

import com.forward.direct.debit.camunda.task.common.TaskContext;

public  class CamundaTaskExecutor extends AbstractBpmnActivityBehavior {

    // abstract TaskDefinition getTaskDefinition();

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String currentActivityId = execution.getCurrentActivityId();

        //TaskDefinition taskDefinition = CamundaSetup.getInstance().getTaskMap().
        CamundaTaskExecutor taskExecutor = CamundaBPMHelper.getTaskExecutor(currentActivityId);
        taskExecutor.
        TaskContext context = new TaskContext(execution);
        taskDefinition.execute(context);
        leave(execution);  // ← this is the key difference from JavaDelegate
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
        // handle async signals / callbacks if needed
        leave(execution);
    }
}