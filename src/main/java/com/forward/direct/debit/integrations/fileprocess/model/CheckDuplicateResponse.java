package com.forward.direct.debit.integrations.fileprocess.model;

/**
 * Returned by POST /file/{fileId}/checkDuplicate on fwb-file-process-service.
 */
public class CheckDuplicateResponse {

    private Long    fileId;
    private Boolean isDuplicate;

    public CheckDuplicateResponse() {}

    public Long    getFileId()               { return fileId; }
    public Boolean getIsDuplicate()          { return isDuplicate; }

    public void setFileId(Long fileId)             { this.fileId      = fileId; }
    public void setIsDuplicate(Boolean isDuplicate){ this.isDuplicate = isDuplicate; }
}
