package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.dto.UserInfoDto;

public interface TrackingService {
    void trackStreamer(UserInfoDto userInfo, TrackingRequestDto trackingRequest);
}
