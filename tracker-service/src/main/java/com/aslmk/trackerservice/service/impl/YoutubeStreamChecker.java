package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.service.PlatformStreamChecker;
import org.springframework.stereotype.Service;

@Service
public class YoutubeStreamChecker implements PlatformStreamChecker {
    @Override
    public boolean isLive(String streamerName) {
        return false;
    }
}
