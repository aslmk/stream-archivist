package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.CreateTrackingSubscriptionDto;
import com.aslmk.trackerservice.dto.DeleteTrackingSubscriptionDto;
import com.aslmk.trackerservice.entity.StreamTrackingSubscriptionEntity;
import com.aslmk.trackerservice.repository.StreamTrackingSubscriptionRepository;
import com.aslmk.trackerservice.service.StreamTrackingSubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StreamTrackingSubscriptionServiceImpl implements StreamTrackingSubscriptionService {
    private final StreamTrackingSubscriptionRepository repository;

    public StreamTrackingSubscriptionServiceImpl(StreamTrackingSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveSubscription(CreateTrackingSubscriptionDto dto) {
        StreamTrackingSubscriptionEntity entity = StreamTrackingSubscriptionEntity.builder()
                .subscriptionId(dto.getSubscriptionId())
                .subscriptionType(dto.getSubscriptionType())
                .providerName(dto.getProviderName())
                .streamerId(dto.getStreamerInternalId())
                .build();

        repository.save(entity);
    }

    @Override
    public void deleteSubscription(DeleteTrackingSubscriptionDto dto) {
        repository.deleteById(dto.getSubscriptionId());
    }

    @Override
    public List<StreamTrackingSubscriptionEntity> getAllSubscriptionsByStreamerId(UUID streamerId) {
        return repository.findAllByStreamerId(streamerId);
    }
}
