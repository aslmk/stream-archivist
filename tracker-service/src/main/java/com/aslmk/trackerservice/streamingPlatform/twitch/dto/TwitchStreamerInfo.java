package com.aslmk.trackerservice.streamingPlatform.twitch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwitchStreamerInfo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("login")
    private String login;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("type")
    private String type;
    @JsonProperty("broadcaster_type")
    private String broadcasterType;
    @JsonProperty("description")
    private String description;
    @JsonProperty("profile_image_url")
    private String profileImageUrl;
    @JsonProperty("offline_image_url")
    private String offlineImageUrl;
    @JsonProperty("created_at")
    private String createdAt;
}
