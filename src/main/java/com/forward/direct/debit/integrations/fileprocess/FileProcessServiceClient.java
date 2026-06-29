package com.forward.direct.debit.integrations.fileprocess;

import com.forward.direct.debit.integrations.fileprocess.model.CheckDuplicateRequest;
import com.forward.direct.debit.integrations.fileprocess.model.CheckDuplicateResponse;
import com.forward.direct.debit.integrations.fileprocess.model.DispatchRequest;
import com.forward.direct.debit.integrations.fileprocess.model.DispatchResponse;
import com.forward.direct.debit.integrations.fileprocess.model.GetMessageIdResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for fwb-file-process-service (http://localhost:8081).
 *
 * Endpoints:
 *   GET  /file/{fileId}/getMessageId      → {@link #getMessageId(long)}
 *   POST /file/{fileId}/checkDuplicate    → {@link #checkDuplicate(long, long, String)}
 *   POST /triggerToMq                     → {@link #dispatch(long, long)}
 */
@Service
public record FileProcessServiceClient(RestTemplate restTemplate) {

    private static final String BASE_URL = "http://localhost:8081";

    // ── Duplicate check ───────────────────────────────────────────────────────

    /**
     * Step 1 of duplicate check: download the payment XML from S3 and extract
     * its MsgId. Calls GET /file/{fileId}/getMessageId?fileS3Path=...
     */
    public GetMessageIdResponse getMessageId(long fileId, String fileS3Path) {
        String url = BASE_URL + "/file/" + fileId + "/getMessageId?fileS3Path="
                + org.springframework.web.util.UriUtils.encodeQueryParam(fileS3Path, "UTF-8");
        System.out.println("[FileProcessServiceClient] GET " + url);
        return restTemplate.getForObject(url, GetMessageIdResponse.class);
    }

    /**
     * Step 2 of duplicate check: attempt to insert (fileId, custId, msgId) and
     * detect duplicates via DataIntegrityViolation on the server side.
     * Calls POST /file/{fileId}/checkDuplicate.
     */
    public CheckDuplicateResponse checkDuplicate(long fileId, long custId, String msgId) {
        String url = BASE_URL + "/file/" + fileId + "/checkDuplicate";
        System.out.println("[FileProcessServiceClient] POST " + url
                + "  custId=" + custId + "  msgId=" + msgId);
        CheckDuplicateRequest request = new CheckDuplicateRequest(fileId, custId, msgId);
        return restTemplate.postForObject(url, request, CheckDuplicateResponse.class);
    }

    // ── Existing dispatch ─────────────────────────────────────────────────────

    public DispatchResponse dispatch(long custId, long fileId) {
        DispatchRequest request = new DispatchRequest(custId, fileId);
        return restTemplate.postForObject(
                BASE_URL + "/triggerToMq",
                request,
                DispatchResponse.class
        );
    }
}
