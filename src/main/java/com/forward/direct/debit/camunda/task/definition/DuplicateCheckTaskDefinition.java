package com.forward.direct.debit.camunda.task.definition;

import com.forward.direct.debit.camunda.model.InputMessage;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.integrations.fileprocess.FileProcessServiceClient;
import com.forward.direct.debit.integrations.fileprocess.model.CheckDuplicateResponse;
import com.forward.direct.debit.integrations.fileprocess.model.GetMessageIdResponse;
import org.springframework.context.ApplicationContext;

/**
 * Duplicate-check service task in the direct-debit-process BPMN workflow.
 *
 * Logic:
 *  1. Read fileId (= fileDataSeq) and custId from the process variables set
 *     during message_validation_task (stored in TRIGGER_MESSAGE).
 *  2. Call GET /file/{fileId}/getMessageId on fwb-file-process-service to
 *     retrieve the payment file's MsgId (pain.008 GrpHdr/MsgId).
 *  3. Call POST /file/{fileId}/checkDuplicate with (fileId, custId, msgId).
 *     The service attempts an INSERT; a DataIntegrityViolation on the
 *     unique index (CUST_ID, MSG_ID) means the file is a duplicate.
 *  4. Set process variable is_file_duplicate accordingly.
 *
 * Downstream gateway condition: ${is_file_duplicate} / ${!is_file_duplicate}
 */
public class DuplicateCheckTaskDefinition extends ServiceTaskDefinition {

    public DuplicateCheckTaskDefinition(ExecutionContext executionContext,
                                        ApplicationContext applicationContext) {
        super(executionContext, applicationContext);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("DuplicateCheckTaskDefinition executing...");

        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // ── Step 1: extract fileId and custId from TRIGGER_MESSAGE ────────────
        InputMessage triggerMessage = (InputMessage) executionContext.getVariable("TRIGGER_MESSAGE");

        if (triggerMessage == null) {
            throw new IllegalStateException(
                    "TRIGGER_MESSAGE process variable is null — message_validation_task must run first");
        }

        long fileId = triggerMessage.fileDataSeq();   // fileDataSeq == fileId
        // custId is not part of the current InputMessage schema; default to 0 until
        // the upstream trigger message is extended to carry it.
        // TODO: add custId to InputMessage and the inbound MQ payload.
        long custId = 0L;

        System.out.println("  fileId (fileDataSeq) : " + fileId);
        System.out.println("  custId               : " + custId);

        FileProcessServiceClient client =
                applicationContext.getBean(FileProcessServiceClient.class);

        // ── Step 2: get the MsgId for this file from fwb-file-process-service ─
        GetMessageIdResponse msgIdResponse = client.getMessageId(fileId);
        String msgId = msgIdResponse != null ? msgIdResponse.getMsgId() : "";

        System.out.println("  msgId from getMessageId: " + msgId);

        // ── Step 3: check for duplicate ───────────────────────────────────────
        CheckDuplicateResponse duplicateResponse =
                client.checkDuplicate(fileId, custId, msgId);

        boolean isDuplicate = duplicateResponse != null
                && Boolean.TRUE.equals(duplicateResponse.getIsDuplicate());

        System.out.println("  isDuplicate: " + isDuplicate);

        // ── Step 4: write result to process variables ─────────────────────────
        setVariable("is_file_duplicate", isDuplicate);

        System.out.println("=".repeat(80));
    }
}
