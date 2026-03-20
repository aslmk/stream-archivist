package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.dto.CreateTrackingSubscriptionDto;
import com.aslmk.trackerservice.dto.DeleteTrackingSubscriptionDto;
import com.aslmk.trackerservice.entity.StreamTrackingSubscriptionEntity;

import java.util.List;
import java.util.UUID;

public interface StreamTrackingSubscriptionService {
    List<StreamTrackingSubscriptionEntity> getAllSubscriptionsByStreamerId(UUID streamerId);
    void saveSubscription(CreateTrackingSubscriptionDto dto);
    void deleteSubscription(DeleteTrackingSubscriptionDto dto);
}
