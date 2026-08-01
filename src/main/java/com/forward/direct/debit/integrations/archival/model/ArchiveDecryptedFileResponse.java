package com.forward.direct.debit.integrations.archival.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body from PUT /v1/fileChannel/archiveDecryptedFileInfo
 * in fwb-file-channel-archival-service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchiveDecryptedFileResponse {

    @JsonProperty("file_sequence_id")
    private Long fileSequenceId;

    @JsonProperty("archived")
    private boolean archived;

    @JsonProperty("message")
    private String message;

    public ArchiveDecryptedFileResponse() {}

    public Long    getFileSequenceId() { return fileSequenceId; }
    public boolean isArchived()        { return archived; }
    public String  getMessage()        { return message; }

    public void setFileSequenceId(Long fileSequenceId) { this.fileSequenceId = fileSequenceId; }
    public void setArchived(boolean archived)           { this.archived = archived; }
    public void setMessage(String message)              { this.message = message; }

    @Override
    public String toString() {
        return "ArchiveDecryptedFileResponse{fileSequenceId=" + fileSequenceId
                + ", archived=" + archived
                + ", message='" + message + "'}";
    }
}
