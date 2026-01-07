package com.aslmk.trackerservice.service;

import java.util.UUID;

public interface StreamerResolutionService {
    UUID resolveStreamerId(String providerUserId, String providerName);
}
