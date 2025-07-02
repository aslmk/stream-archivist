package com.aslmk.trackerservice.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class YoutubeLiveStatusDto {
    @JsonProperty("items")
    private Item[] items;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Item {
        @JsonProperty("liveBroadCastContent")
        private String liveBroadCastContent;
    }
}