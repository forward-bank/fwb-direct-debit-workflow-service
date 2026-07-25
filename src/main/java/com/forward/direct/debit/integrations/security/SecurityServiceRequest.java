package com.forward.direct.debit.integrations.security;

public record SecurityServiceRequest(Long custId, String fileS3Path, boolean pgpSigningEnabled) {
}
