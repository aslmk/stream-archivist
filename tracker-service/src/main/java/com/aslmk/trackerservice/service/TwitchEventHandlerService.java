package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEventSubRequest;

public interface TwitchEventHandlerService {
    void handle(TwitchEventSubRequest request);
}
