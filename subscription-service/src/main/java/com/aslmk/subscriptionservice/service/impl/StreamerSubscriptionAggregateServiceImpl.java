package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.subscriptionservice.entity.StreamerSubscriptionAggregateEntity;
import com.aslmk.subscriptionservice.repository.StreamerSubscriptionAggregateRepository;
import com.aslmk.subscriptionservice.service.StreamerSubscriptionAggregateService;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StreamerSubscriptionAggregateServiceImpl implements StreamerSubscriptionAggregateService {

    private final StreamerSubscriptionAggregateRepository repository;

    public StreamerSubscriptionAggregateServiceImpl(StreamerSubscriptionAggregateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public int incrementOrCreate(UUID streamerId) {
        try {
            StreamerSubscriptionAggregateEntity entity = StreamerSubscriptionAggregateEntity.builder()
                    .streamerId(streamerId)
                    .subscriptionCount(1)
                    .build();
            StreamerSubscriptionAggregateEntity savedEntity = repository.save(entity);
            return savedEntity.getSubscriptionCount();
        } catch (DataIntegrityViolationException e) {
            return repository.incrementSubscriptionCountAndGetSubscriptionCountByStreamerId(streamerId);
        }
    }

    @Override
    public int decrementSubscriptionCount(UUID streamerId) {
        return repository.decrementSubscriptionCountAndGetSubscriptionCountByStreamerId(streamerId);
    }
}
