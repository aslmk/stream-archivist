package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.dto.TrackingRequestDto;

public interface TrackingService {
    void trackStreamer(TrackingRequestDto trackingRequest);
}
