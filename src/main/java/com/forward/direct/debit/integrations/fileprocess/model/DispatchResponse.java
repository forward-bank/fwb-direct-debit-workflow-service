package com.forward.direct.debit.integrations.fileprocess.model;

public class DispatchResponse {
    private Integer numberOfTxnDispatched;

    public DispatchResponse(Integer numberOfTxnDispatched) {
        this.numberOfTxnDispatched = numberOfTxnDispatched;
    }
}
