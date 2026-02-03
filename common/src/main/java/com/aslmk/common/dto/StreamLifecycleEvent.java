package com.aslmk.common.dto;

import com.aslmk.common.constants.StreamLifecycleType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamLifecycleEvent {
    private StreamLifecycleType eventType;
    private String streamerUsername;
    private String streamUrl;
    private UUID streamerId;
}
