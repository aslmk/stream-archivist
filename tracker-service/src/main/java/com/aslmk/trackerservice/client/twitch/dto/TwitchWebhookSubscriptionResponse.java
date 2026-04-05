package com.aslmk.trackerservice.client.twitch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
public class TwitchWebhookSubscriptionResponse {
    private UUID id;
    private String type;
    private String status;
}
