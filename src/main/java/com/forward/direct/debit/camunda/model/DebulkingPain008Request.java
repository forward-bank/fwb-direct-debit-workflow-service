package com.forward.direct.debit.camunda.model;


import java.io.Serializable;

public class DebulkingPain008Request implements Serializable {

     private long custId;
     private long fileId;
     private String fileS3Path;

    public DebulkingPain008Request(long custId, long fileId, String fileS3Path) {
        this.custId = custId;
        this.fileId = fileId;
        this.fileS3Path = fileS3Path;
    }

    public long getCustId()       { return custId; }
    public long getFileId()       { return fileId; }
    public String getFileS3Path() { return fileS3Path; }

}
