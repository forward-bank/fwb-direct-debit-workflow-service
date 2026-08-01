package com.forward.direct.debit.integrations.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Deserialized response received from {@code SECURITY.SERVICE.RESPONSE.QUEUE}.
 *
 * <p>JSON contract from fwb-security-service:
 * <pre>
 * // Success
 * {
 *   "custId"            : 1001,
 *   "decrypted"         : true,
 *   "decryptedFilePath" : "forward-bank-payments/FWB_DIRECT_DEBIT/PAYMENT_FILES/2026/02/04/DECRYPTED/I1234567890123.FWB.pain00800108.ABCD123.PM.xml"
 * }
 *
 * // Failure
 * {
 *   "custId"       : 1001,
 *   "decrypted"    : false,
 *   "errorCode"    : "SSE_002",
 *   "errorMessage" : "No matching bank private key found ..."
 * }
 * </pre>
 *
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} ensures the model is
 * forward-compatible if fwb-security-service adds new fields later.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityServiceResponse {

    @JsonProperty("custId")
    private Long custId;

    @JsonProperty("decrypted")
    private boolean decrypted;

    @JsonProperty("decryptedFilePath")
    private String decryptedFilePath;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("errorMessage")
    private String errorMessage;

    // ── Default constructor required by Jackson ───────────────────────────────
    public SecurityServiceResponse() {}

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long    getCustId()            { return custId; }
    public boolean isDecrypted()          { return decrypted; }
    public String  getDecryptedFilePath() { return decryptedFilePath; }
    public String  getErrorCode()         { return errorCode; }
    public String  getErrorMessage()      { return errorMessage; }

    // ── Setters (required by Jackson for deserialization) ─────────────────────

    public void setCustId(Long custId)                        { this.custId = custId; }
    public void setDecrypted(boolean decrypted)               { this.decrypted = decrypted; }
    public void setDecryptedFilePath(String decryptedFilePath){ this.decryptedFilePath = decryptedFilePath; }
    public void setErrorCode(String errorCode)                { this.errorCode = errorCode; }
    public void setErrorMessage(String errorMessage)          { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        return "SecurityServiceResponse{custId=" + custId
                + ", decrypted=" + decrypted
                + ", decryptedFilePath='" + decryptedFilePath
                + "', errorCode='" + errorCode + "'}";
    }
}
