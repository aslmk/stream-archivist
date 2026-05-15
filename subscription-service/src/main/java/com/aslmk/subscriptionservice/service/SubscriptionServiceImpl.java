package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.domain.SubscriptionEntity;
import com.aslmk.subscriptionservice.dto.CreateSubscriptionDto;
import com.aslmk.subscriptionservice.dto.TrackedStreamerDto;
import com.aslmk.subscriptionservice.repository.SubscriptionRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository repository;

    public SubscriptionServiceImpl(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean subscribe(CreateSubscriptionDto dto) {
        if (dto.getSubscriberId().equals(dto.getStreamerId())) {
            throw new IllegalArgumentException(String.format(
                    "Subscriber can't subscribe to himself: userId='%s', streamerId='%s'",
                    dto.getSubscriberId(), dto.getStreamerId()));
        }

        try {
            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .userId(dto.getSubscriberId())
                    .streamerId(dto.getStreamerId())
                    .build();

            repository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                return false;
            }

            throw new IllegalStateException(String.format(
                    "Unexpected DB error while subscribing: userId='%s', streamerId='%s'",
                    dto.getSubscriberId(), dto.getStreamerId()), e);
        }
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
