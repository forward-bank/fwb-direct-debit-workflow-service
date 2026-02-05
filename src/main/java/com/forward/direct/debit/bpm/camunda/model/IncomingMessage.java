package com.forward.direct.debit.bpm.camunda.model;


public record IncomingMessage (Long fileDataSeq,
                               String channelRef,
                               String outputChannelCode,
                               String fileS3Path){

}
