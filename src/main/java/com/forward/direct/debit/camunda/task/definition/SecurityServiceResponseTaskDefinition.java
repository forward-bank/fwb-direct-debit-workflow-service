package com.forward.direct.debit.camunda.task.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forward.direct.debit.camunda.task.common.ExecutionContext;
import com.forward.direct.debit.integrations.security.SecurityServiceResponse;

import javax.jms.Message;

/**
 * Receive task that handles the decryption response from fwb-security-service.
 *
 * <p>Reads the JSON response from {@code SECURITY.SERVICE.RESPONSE.QUEUE} and
 * deserializes it into a {@link SecurityServiceResponse}.
 *
 * <p>Sets the following Camunda process variables:
 * <ul>
 *   <li>{@code is_file_decrypted_successfully} — {@code boolean}, mirrors
 *       {@link SecurityServiceResponse#isDecrypted()}.</li>
 *   <li>{@code DECRYPTED_FILE_PATH} — {@code String}, the S3 path of the
 *       decrypted file. Set only when {@code decrypted == true}; left unset
 *       on failure so downstream tasks can guard against a missing path.</li>
 * </ul>
 *
 * <p>Downstream BPMN gateway conditions:
 * <pre>
 *   ${is_file_decrypted_successfully}   → proceed to syntax validation
 *   ${!is_file_decrypted_successfully}  → end / error path
 * </pre>
 */
public class SecurityServiceResponseTaskDefinition extends MessageReceiveTaskDefinition {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SecurityServiceResponseTaskDefinition(ExecutionContext executionContext) {
        super(executionContext);
    }

    @Override
    public void execute(Message message) throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("SecurityServiceResponseTaskDefinition: executing...");

        System.out.println("Execution Context Variables:");
        executionContext.getVariables().forEach((key, value) ->
                System.out.println("  " + key + ": " + value));

        // ── Parse response JSON → SecurityServiceResponse ─────────────────────
        String messageContent = message.getBody(String.class);
        System.out.println("Received JMS Message:");
        System.out.println("  " + messageContent);

        SecurityServiceResponse response =
                OBJECT_MAPPER.readValue(messageContent, SecurityServiceResponse.class);

        System.out.println("  Parsed response : " + response);

        // ── Set process variable: is_file_decrypted_successfully ──────────────
        // Always set so the downstream exclusive gateway can evaluate the condition.
        setVariable("is_file_decrypted_successfully", response.isDecrypted());

        // ── Set process variable: DECRYPTED_FILE_PATH ─────────────────────────
        // Only set when decryption succeeded and the path is present.
        if (response.isDecrypted()
                && response.getDecryptedFilePath() != null
                && !response.getDecryptedFilePath().isBlank()) {
            setVariable("DECRYPTED_FILE_PATH", response.getDecryptedFilePath());
        }

        // ── Logging ───────────────────────────────────────────────────────────
        System.out.println("  ✓ Process variables set:");
        System.out.println("      is_file_decrypted_successfully = " + response.isDecrypted());
        if (response.isDecrypted()) {
            System.out.println("      DECRYPTED_FILE_PATH            = " + response.getDecryptedFilePath());
        } else {
            System.out.println("      DECRYPTED_FILE_PATH            = (not set — decryption failed)");
            System.err.println("  ✗ Decryption failed"
                    + " | errorCode="    + response.getErrorCode()
                    + " | errorMessage=" + response.getErrorMessage());
        }

        System.out.println("=".repeat(80));
    }
}
