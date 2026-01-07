package com.aslmk.common.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntityIdResolveResponse {
    private UUID entityId;
}
