package com.aslmk.subscriptionservice.dto;

import java.util.UUID;

public record UserSubscriptionDto(
        UUID streamerId,
        String streamerUsername,
        String streamerProfileImageUrl,
        String providerName) {}
