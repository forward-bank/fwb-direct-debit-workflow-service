package com.forward.direct.debit.integrations.archival.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for PUT /v1/fileChannel/archiveDecryptedFileInfo
 * in fwb-file-channel-archival-service.
 */
public class ArchiveDecryptedFileRequest {

    @JsonProperty("file_sequence_id")
    private Long fileSequenceId;

    @JsonProperty("channel_ref")
    private String channelRef;

    @JsonProperty("cust_file_s3_path")
    private String custFileS3Path;

    @JsonProperty("decrypted_file_path")
    private String decryptedFilePath;

    public ArchiveDecryptedFileRequest() {}

    public ArchiveDecryptedFileRequest(Long fileSequenceId,
                                       String channelRef,
                                       String custFileS3Path,
                                       String decryptedFilePath) {
        this.fileSequenceId  = fileSequenceId;
        this.channelRef      = channelRef;
        this.custFileS3Path  = custFileS3Path;
        this.decryptedFilePath = decryptedFilePath;
    }

    public Long   getFileSequenceId()   { return fileSequenceId; }
    public String getChannelRef()        { return channelRef; }
    public String getCustFileS3Path()    { return custFileS3Path; }
    public String getDecryptedFilePath() { return decryptedFilePath; }

    public void setFileSequenceId(Long fileSequenceId)         { this.fileSequenceId = fileSequenceId; }
    public void setChannelRef(String channelRef)                { this.channelRef = channelRef; }
    public void setCustFileS3Path(String custFileS3Path)        { this.custFileS3Path = custFileS3Path; }
    public void setDecryptedFilePath(String decryptedFilePath)  { this.decryptedFilePath = decryptedFilePath; }

    @Override
    public String toString() {
        return "ArchiveDecryptedFileRequest{fileSequenceId=" + fileSequenceId
                + ", channelRef='" + channelRef
                + "', custFileS3Path='" + custFileS3Path
                + "', decryptedFilePath='" + decryptedFilePath + "'}";
    }
}
