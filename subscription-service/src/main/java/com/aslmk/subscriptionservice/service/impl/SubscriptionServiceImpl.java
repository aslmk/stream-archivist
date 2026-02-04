package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.common.dto.EntityIdResolveResponse;
import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.entity.SubscriptionEntity;
import com.aslmk.subscriptionservice.repository.SubscriptionRepository;
import com.aslmk.subscriptionservice.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository repository;

    public SubscriptionServiceImpl(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SubscriptionEntity> findByUserId(UUID userId) {
        log.debug("Searching for user with id='{}'", userId);
        Optional<SubscriptionEntity> result = repository.findByUserId(userId);

        if (result.isPresent()) {
            log.debug("Found user with id='{}'", userId);
        } else {
            log.debug("No user with id='{}'", userId);
        }

        return result;
    }

    @Override
    public Optional<SubscriptionEntity> findByStreamerId(UUID streamerId) {
        log.debug("Searching for streamer with id='{}'", streamerId);
        Optional<SubscriptionEntity> result = repository.findByStreamerId(streamerId);

        if (result.isPresent()) {
            log.debug("Found streamer with id='{}'", streamerId);
        } else {
            log.debug("No streamer with id='{}'", streamerId);
        }

        return result;
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
    public List<EntityIdResolveResponse> getAllTrackedStreamers(String userId) {
        UUID uuidUserId = UUID.fromString(userId);

        List<SubscriptionEntity> list = repository.getAllByUserId(uuidUserId);

        List<EntityIdResolveResponse> trackedStreamers = new ArrayList<>();

        list.forEach(sub -> trackedStreamers
                .add(EntityIdResolveResponse.builder()
                        .entityId(sub.getStreamerId())
                        .build())
        );

        return trackedStreamers;
    }
}
