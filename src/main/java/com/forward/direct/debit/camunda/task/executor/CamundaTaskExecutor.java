package com.forward.direct.debit.camunda.task.executor;

import com.forward.direct.debit.camunda.CamundaBPMHelper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.camunda.task.common.ExecutionContextImpl;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

public  class CamundaTaskExecutor extends AbstractBpmnActivityBehavior {

//    private TaskContext buildTaskContext(DelegateExecution delegateExecution) {
//        return new TaskContext(delegateExecution); // pass necessary data
//    }

    private ExecutionContext buildExecutionContext(DelegateExecution delegateExecution) {
        return new ExecutionContextImpl(delegateExecution.getVariables());
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        String currentActivityId = execution.getCurrentActivityId();

        //TaskDefinition taskDefinition = CamundaSetup.getInstance().getTaskMap().
        TaskExecutor taskExecutor = CamundaBPMHelper.getTaskExecutor(currentActivityId);
        // build TaskContext
        //TaskContext taskContext = buildTaskContext(execution);
        ExecutionContext executionContext = buildExecutionContext(execution);
        taskExecutor.executeTask(executionContext);
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