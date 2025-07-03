package com.aslmk.trackerservice.entity;

import com.aslmk.trackerservice.exception.InvalidStreamingPlatformException;

public enum StreamingPlatform {
    YOUTUBE,
    TWITCH;

    public static StreamingPlatform fromString(String platform) {
        if (platform == null) {
            throw new InvalidStreamingPlatformException("Platform cannot be null");
        }

        try {
            return valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStreamingPlatformException("Invalid streaming platform: " + platform);
        }
    }
}
