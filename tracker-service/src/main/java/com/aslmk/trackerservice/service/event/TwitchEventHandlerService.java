package com.aslmk.trackerservice.service.event;

import com.aslmk.trackerservice.client.twitch.dto.TwitchEventSubRequest;

public interface TwitchEventHandlerService {
    void handle(TwitchEventSubRequest request);
}
