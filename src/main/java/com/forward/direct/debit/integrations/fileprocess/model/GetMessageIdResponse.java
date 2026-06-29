package com.forward.direct.debit.integrations.fileprocess.model;

/**
 * Returned by GET /file/{fileId}/getMessageId on fwb-file-process-service.
 * Plain class (not record) so extra fields can be added later without
 * breaking Jackson deserialization.
 */
public class GetMessageIdResponse {

    private String msgId;

    public GetMessageIdResponse() {}

    public String getMsgId()             { return msgId; }
    public void   setMsgId(String msgId) { this.msgId = msgId; }
}
