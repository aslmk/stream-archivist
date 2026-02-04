package com.aslmk.streamstatusservice.client;

import java.util.UUID;

public interface AuthServiceClient {
    UUID resolveUserId(String providerUserId, String providerName);
}
