package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.repository.StreamerSubscriptionAggregateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class StreamerSubscriptionAggregateServiceImpl implements StreamerSubscriptionAggregateService {

    private final StreamerSubscriptionAggregateRepository repository;

    public StreamerSubscriptionAggregateServiceImpl(StreamerSubscriptionAggregateRepository repository) {
        this.repository = repository;
    }

    @Override
    public void incrementOrCreate(UUID streamerId) {
        repository.incrementOrCreate(streamerId);
    }

    @Override
    public void decrementSubscriptionsCount(UUID streamerId) {
        repository.decrementByStreamerId(streamerId);
    }

    @Override
    public int getSubscriptionsCount(UUID streamerId) {
        return repository.getSubscriptionsCountByStreamerId(streamerId)
                .orElse(0);
    }
}
