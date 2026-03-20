package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.dto.TrackedStreamerDto;
import com.aslmk.subscriptionservice.entity.SubscriptionEntity;
import com.aslmk.subscriptionservice.repository.SubscriptionRepository;
import com.aslmk.subscriptionservice.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository repository;

    public SubscriptionServiceImpl(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void subscribe(CreateSubscriptionDto dto) {
        log.info("Creating subscription: subscriberId='{}', streamerId='{}'",
                dto.getSubscriberId(), dto.getStreamerId());

        if (dto.getSubscriberId().equals(dto.getStreamerId())) {
            log.warn("Invalid subscription attempt: subscriberId='{}' equals streamerId='{}'",
                    dto.getSubscriberId(), dto.getStreamerId());
            throw new IllegalArgumentException("Subscriber can't subscribe to himself");
        }

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .userId(dto.getSubscriberId())
                .streamerId(dto.getStreamerId())
                .build();

        repository.save(entity);

        log.info("Subscription created successfully: subscriberId='{}', streamerId='{}'",
                dto.getSubscriberId(), dto.getStreamerId());
    }

    @Override
    public List<TrackedStreamerDto> getAllTrackedStreamers(String userId) {
        UUID uuidUserId = UUID.fromString(userId);

        List<SubscriptionEntity> list = repository.getAllByUserId(uuidUserId);

        List<TrackedStreamerDto> trackedStreamers = new ArrayList<>();

        list.forEach(sub -> trackedStreamers
                .add(TrackedStreamerDto.builder()
                        .id(sub.getStreamerId())
                        .build())
        );

        return trackedStreamers;
    }

    @Override
    public void unsubscribe(String userId, String streamerId) {
        UUID uuidUserId = UUID.fromString(userId);
        UUID uuidStreamerId = UUID.fromString(streamerId);

        repository.deleteByUserIdAndStreamerId(uuidUserId, uuidStreamerId);
    }
}
