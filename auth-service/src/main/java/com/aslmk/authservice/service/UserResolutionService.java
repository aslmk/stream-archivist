package com.aslmk.authservice.service;

import java.util.UUID;

public interface UserResolutionService {
    UUID resolveUserId(String providerUserId, String providerName);
}
