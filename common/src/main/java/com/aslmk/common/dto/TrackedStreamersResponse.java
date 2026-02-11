package com.aslmk.common.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrackedStreamersResponse {
    private List<TrackedStreamerDto> streamers;
}
