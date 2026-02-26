package com.aslmk.streamstatusservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackedStreamerDto {
    private UUID id;
}
