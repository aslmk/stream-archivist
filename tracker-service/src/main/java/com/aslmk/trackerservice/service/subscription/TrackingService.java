package com.aslmk.trackerservice.service.subscription;

import com.aslmk.trackerservice.dto.TrackStreamerResponse;
import com.aslmk.trackerservice.dto.TrackingRequestDto;

public interface TrackingService {
    TrackStreamerResponse trackStreamer(TrackingRequestDto trackingRequest);
    void unsubscribe(String streamerId);
}
