package com.aslmk.subscriptionservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackStreamerResponse {
    private UUID streamerId;
    private String streamerUsername;
    private String streamerProfileImageUrl;
    private String providerName;
}
