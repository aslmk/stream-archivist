package com.aslmk.common.dto;

import com.aslmk.common.constants.StreamLifecycleType;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamLifecycleEvent {
    private StreamLifecycleType eventType;
    private String streamerUsername;
    private String streamUrl;
    private String providerName;
    private String providerUserId;
}
