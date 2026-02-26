package com.aslmk.subscriptionservice.dto;

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
