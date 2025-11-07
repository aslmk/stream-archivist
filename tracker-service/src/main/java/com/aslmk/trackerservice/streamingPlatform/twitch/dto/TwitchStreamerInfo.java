package com.aslmk.trackerservice.streamingPlatform.twitch.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwitchStreamerInfo {
    @JsonAlias("id")
    private String id;
    @JsonAlias("login")
    private String login;
    @JsonAlias("display_name")
    private String displayName;
    @JsonAlias("type")
    private String type;
    @JsonAlias("broadcaster_type")
    private String broadcasterType;
    @JsonAlias("description")
    private String description;
    @JsonAlias("profile_image_url")
    private String profileImageUrl;
    @JsonAlias("offline_image_url")
    private String offlineImageUrl;
    @JsonAlias("created_at")
    private String createdAt;
}
