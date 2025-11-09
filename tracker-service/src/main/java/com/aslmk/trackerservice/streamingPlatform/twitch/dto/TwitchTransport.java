package com.aslmk.trackerservice.streamingPlatform.twitch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwitchTransport {
    @JsonProperty("method")
    private String method;
    @JsonProperty("callback")
    private String callback;
    @JsonProperty("secret")
    private String secret;
}
