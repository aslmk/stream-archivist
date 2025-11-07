package com.aslmk.trackerservice.streamingPlatform.twitch.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwitchTransport {
    @JsonAlias("method")
    private String method;
    @JsonAlias("callback")
    private String callback;
    @JsonAlias("secret")
    private String secret;
}
