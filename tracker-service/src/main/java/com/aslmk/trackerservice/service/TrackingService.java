package com.aslmk.trackerservice.service;

import com.aslmk.common.dto.TrackStreamerResponse;
import com.aslmk.common.dto.TrackingRequestDto;

public interface TrackingService {
    TrackStreamerResponse trackStreamer(TrackingRequestDto trackingRequest);
}
