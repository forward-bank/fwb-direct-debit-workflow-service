package com.forward.direct.debit.camunda.model;

public record InputMessage(
        Long fileDataSeq,
        String channelRef,
        String outputChannelCode,
        String fileS3Path
){}
