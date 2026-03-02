package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.CamundaBPMHelper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.ExecutionContextImpl;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

import com.forward.direct.debit.camunda.task.common.TaskContext;

public  class CamundaTaskExecutor extends AbstractBpmnActivityBehavior {

    // abstract TaskDefinition getTaskDefinition();

    private ExecutionContext getExecutionContext(DelegateExecution delegateExecution) {
        // build and return an ExecutionContext based on the execution
        ExecutionContext executionContext = new ExecutionContextImpl();
        return executionContext;
    }
    private TaskContext buildTaskContext(DelegateExecution delegateExecution) {
        ExecutionContext executionContext = getExecutionContext(delegateExecution);
        TaskContext taskContext = new TaskContext(delegateExecution, executionContext);
        return taskContext; // pass necessary data
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String currentActivityId = execution.getCurrentActivityId();

        //TaskDefinition taskDefinition = CamundaSetup.getInstance().getTaskMap().
        TaskExecutor taskExecutor = CamundaBPMHelper.getTaskExecutor(currentActivityId);
        // build TaskContext
        TaskContext taskContext = buildTaskContext(execution);

        taskExecutor.executeTask(taskContext);
//        TaskContext context = new TaskContext(execution);
//        taskDefinition.execute(context);
        //leave(execution);  // ← this is the key difference from JavaDelegate
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
        // handle async signals / callbacks if needed
        leave(execution);
    }
}