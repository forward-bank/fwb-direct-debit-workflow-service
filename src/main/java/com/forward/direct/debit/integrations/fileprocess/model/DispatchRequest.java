package com.forward.direct.debit.integrations.fileprocess.model;

public class DispatchRequest{
    private Long custId;
    private Long fileId;

    public DispatchRequest(long custId, long fileId) {
        this.custId = custId;
        this.fileId = fileId;
    }

    public long getCustId()       { return custId; }
    public long getFileId()       { return fileId; }
}
