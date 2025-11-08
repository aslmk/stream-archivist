package com.aslmk.trackerservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingRequestDto {
    private String streamerUsername;
    private String streamQuality;
    private String providerName;
}
