package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.service.PlatformStreamChecker;
import com.aslmk.trackerservice.streamingPlatform.youtube.YoutubeApiClient;
import org.springframework.stereotype.Service;

@Service
public class YoutubeStreamChecker implements PlatformStreamChecker {

    private final YoutubeApiClient youtubeApiClient;

    public YoutubeStreamChecker(YoutubeApiClient youtubeApiClient) {
        this.youtubeApiClient = youtubeApiClient;
    }

    @Override
    public boolean isLive(String broadcasterUsername) {
        return youtubeApiClient.isChannelLive(broadcasterUsername);
    }
}
