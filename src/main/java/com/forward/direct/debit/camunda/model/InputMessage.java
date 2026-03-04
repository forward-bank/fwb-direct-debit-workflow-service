package com.forward.direct.debit.camunda.model;

import java.io.Serializable;

public record InputMessage(
        Long fileDataSeq,
        String channelRef,
        String outputChannelCode,
        String fileS3Path
) implements Serializable {}
