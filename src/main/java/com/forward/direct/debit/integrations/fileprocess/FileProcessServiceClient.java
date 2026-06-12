package com.forward.direct.debit.integrations.fileprocess;

import com.forward.direct.debit.integrations.fileprocess.model.DispatchRequest;
import com.forward.direct.debit.integrations.fileprocess.model.DispatchResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public record FileProcessServiceClient(RestTemplate restTemplate) {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String DISPATCH_ENDPOINT = "/v1/triggerToMq";

    public DispatchResponse dispatch(long custId, long fileId) {
        DispatchRequest request = new DispatchRequest(custId, fileId);
        return restTemplate.postForObject(
                BASE_URL + DISPATCH_ENDPOINT,
                request,
                DispatchResponse.class
        );
    }
}