package com.aslmk.subscriptionservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequest {
    @NotEmpty
    private String streamerUsername;
    @NotEmpty
    private String providerName;
}
