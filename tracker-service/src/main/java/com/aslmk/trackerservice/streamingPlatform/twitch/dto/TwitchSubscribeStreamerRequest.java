package com.aslmk.trackerservice.streamingPlatform.twitch.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwitchSubscribeStreamerRequest {
    @JsonAlias("type")
    private String type;
    @JsonAlias("version")
    private String version;
    @JsonAlias("condition")
    private TwitchCondition condition;
    @JsonAlias("transport")
    private TwitchTransport transport;
}
