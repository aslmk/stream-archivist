package com.aslmk.trackerservice.service;

import com.aslmk.common.dto.TrackingRequestDto;

import java.util.UUID;

public interface TrackingService {
    UUID trackStreamer(TrackingRequestDto trackingRequest);
}
