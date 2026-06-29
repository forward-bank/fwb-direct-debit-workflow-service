package com.forward.direct.debit.integrations.fileprocess.model;

/**
 * Request body for POST /file/{fileId}/checkDuplicate on fwb-file-process-service.
 */
public class CheckDuplicateRequest {

    private Long   fileId;
    private Long   custId;
    private String msgId;

    public CheckDuplicateRequest() {}

    public CheckDuplicateRequest(Long fileId, Long custId, String msgId) {
        this.fileId = fileId;
        this.custId = custId;
        this.msgId  = msgId;
    }

    public Long   getFileId()          { return fileId; }
    public Long   getCustId()          { return custId; }
    public String getMsgId()           { return msgId; }

    public void setFileId(Long fileId)    { this.fileId = fileId; }
    public void setCustId(Long custId)    { this.custId = custId; }
    public void setMsgId(String msgId)    { this.msgId  = msgId; }
}
