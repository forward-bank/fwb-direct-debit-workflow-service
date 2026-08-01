package com.forward.direct.debit.camunda.task.definition;

import com.forward.direct.debit.camunda.model.InputMessage;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.integrations.archival.FileChannelArchivalServiceClient;
import com.forward.direct.debit.integrations.archival.model.ArchiveDecryptedFileResponse;
import org.springframework.context.ApplicationContext;

/**
 * Service task that archives decrypted payment file metadata by calling
 * PUT /v1/fileChannel/archiveDecryptedFileInfo on fwb-file-channel-archival-service.
 *
 * <p>Process variables consumed (set by earlier tasks):
 * <ul>
 *   <li>{@code TRIGGER_MESSAGE} — {@link InputMessage} carrying
 *       {@code fileDataSeq}, {@code channelRef}, and {@code fileS3Path}</li>
 *   <li>{@code DECRYPTED_FILE_PATH} — set by
 *       {@link SecurityServiceResponseTaskDefinition} after successful decryption</li>
 * </ul>
 *
 * <p>Request body sent to fwb-file-channel-archival-service:
 * <pre>
 * {
 *   "file_sequence_id"   : &lt;fileDataSeq from TRIGGER_MESSAGE&gt;,
 *   "channel_ref"        : &lt;channelRef from TRIGGER_MESSAGE&gt;,
 *   "cust_file_s3_path"  : &lt;fileS3Path from TRIGGER_MESSAGE&gt;,
 *   "decrypted_file_path": &lt;DECRYPTED_FILE_PATH process variable&gt;
 * }
 * </pre>
 */
public class ArchivalTaskDefinition extends ServiceTaskDefinition {

    public ArchivalTaskDefinition(ExecutionContext executionContext,
                                  ApplicationContext applicationContext) {
        super(executionContext, applicationContext);
    }

    @Override
    public void execute() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("ArchivalTaskDefinition: executing...");

        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) ->
                System.out.println("  " + key + ": " + value));

        // ── Step 1: read required data from process variables ─────────────────
        InputMessage triggerMessage =
                (InputMessage) executionContext.getVariable("TRIGGER_MESSAGE");

        if (triggerMessage == null) {
            throw new IllegalStateException(
                    "TRIGGER_MESSAGE process variable is null — "
                    + "message_validation_task must run before archival_task");
        }

        Long   fileSequenceId = triggerMessage.fileDataSeq();
        String channelRef     = triggerMessage.channelRef();
        String custFileS3Path = triggerMessage.fileS3Path();

        String decryptedFilePath =
                (String) executionContext.getVariable("DECRYPTED_FILE_PATH");

        if (decryptedFilePath == null || decryptedFilePath.isBlank()) {
            throw new IllegalStateException(
                    "DECRYPTED_FILE_PATH process variable is null or blank — "
                    + "security_service_response_task must run before archival_task");
        }

        System.out.println("  fileSequenceId   : " + fileSequenceId);
        System.out.println("  channelRef       : " + channelRef);
        System.out.println("  custFileS3Path   : " + custFileS3Path);
        System.out.println("  decryptedFilePath: " + decryptedFilePath);

        // ── Step 2: call fwb-file-channel-archival-service ────────────────────
        FileChannelArchivalServiceClient client =
                applicationContext.getBean(FileChannelArchivalServiceClient.class);

        ArchiveDecryptedFileResponse response = client.archiveDecryptedFileInfo(
                fileSequenceId,
                channelRef,
                custFileS3Path,
                decryptedFilePath);

        // ── Step 3: validate response ─────────────────────────────────────────
        boolean archived = response != null && response.isArchived();

        if (!archived) {
            System.err.println("  ✗ Archival failed — response: " + response);
            throw new RuntimeException(
                    "fwb-file-channel-archival-service returned archived=false "
                    + "for fileSequenceId=" + fileSequenceId);
        }

        System.out.println("  ✓ Archival succeeded — fileSequenceId=" + fileSequenceId);
        System.out.println("=".repeat(80));
    }
}
