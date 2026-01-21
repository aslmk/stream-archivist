package com.aslmk.common.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordingRequestDto {
    private String streamerUsername;
    private String streamUrl;
    private String providerName;
    private String providerUserId;
}
