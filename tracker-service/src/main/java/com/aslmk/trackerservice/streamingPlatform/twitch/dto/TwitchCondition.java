package com.aslmk.trackerservice.streamingPlatform.twitch.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwitchCondition {
    @JsonAlias("broadcaster_user_id")
    private String broadcasterUserId;
}
