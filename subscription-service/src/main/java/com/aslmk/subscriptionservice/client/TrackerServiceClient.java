package com.aslmk.subscriptionservice.client;

import java.util.UUID;

public interface TrackerServiceClient {
    UUID resolveStreamerId(String providerUserId, String providerName);
}
