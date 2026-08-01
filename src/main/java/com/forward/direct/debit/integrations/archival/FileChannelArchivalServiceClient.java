package com.forward.direct.debit.integrations.archival;

import com.forward.direct.debit.integrations.archival.model.ArchiveDecryptedFileRequest;
import com.forward.direct.debit.integrations.archival.model.ArchiveDecryptedFileResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for fwb-file-channel-archival-service (http://localhost:8086).
 *
 * Endpoints:
 *   PUT /v1/fileChannel/archiveDecryptedFileInfo → {@link #archiveDecryptedFileInfo}
 */
@Service
public record FileChannelArchivalServiceClient(RestTemplate restTemplate) {

    private static final String BASE_URL     = "http://localhost:8086";
    private static final String ARCHIVE_PATH = "/v1/fileChannel/archiveDecryptedFileInfo";

    /**
     * Calls PUT /v1/fileChannel/archiveDecryptedFileInfo on fwb-file-channel-archival-service
     * to persist decrypted file metadata.
     *
     * @param fileSequenceId   the file data sequence ID (maps to file_sequence_id)
     * @param channelRef       channel reference from the original trigger message
     * @param custFileS3Path   full S3 path of the encrypted payment file
     * @param decryptedFilePath full S3 path of the decrypted payment file
     * @return {@link ArchiveDecryptedFileResponse} from the archival service
     */
    public ArchiveDecryptedFileResponse archiveDecryptedFileInfo(Long   fileSequenceId,
                                                                  String channelRef,
                                                                  String custFileS3Path,
                                                                  String decryptedFilePath) {
        String url = BASE_URL + ARCHIVE_PATH;

        ArchiveDecryptedFileRequest request = new ArchiveDecryptedFileRequest(
                fileSequenceId,
                channelRef,
                custFileS3Path,
                decryptedFilePath);

        System.out.println("[FileChannelArchivalServiceClient] PUT " + url
                + " | fileSequenceId=" + fileSequenceId
                + " | channelRef=" + channelRef);

        ResponseEntity<ArchiveDecryptedFileResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        new HttpEntity<>(request),
                        ArchiveDecryptedFileResponse.class);

        System.out.println("[FileChannelArchivalServiceClient] ✓ response: " + response.getBody());
        return response.getBody();
    }
}
