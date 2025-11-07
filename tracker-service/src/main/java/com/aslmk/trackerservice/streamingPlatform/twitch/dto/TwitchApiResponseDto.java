package com.aslmk.trackerservice.streamingPlatform.twitch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TwitchApiResponseDto {
    private List<TwitchStreamerInfo> data;
}
