package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.dto.TrackedStreamerDto;

import java.util.List;

public interface SubscriptionService {
    void subscribe(CreateSubscriptionDto dto);
    List<TrackedStreamerDto> getAllTrackedStreamers(String userId);
}
