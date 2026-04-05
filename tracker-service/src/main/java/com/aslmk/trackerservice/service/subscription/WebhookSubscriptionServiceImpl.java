package com.aslmk.trackerservice.service.subscription;

import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import com.aslmk.trackerservice.dto.WebhookSubscriptionDto;
import com.aslmk.trackerservice.dto.WebhookSubscriptionStatus;
import com.aslmk.trackerservice.repository.WebhookSubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WebhookSubscriptionServiceImpl implements WebhookSubscriptionService {
    private final WebhookSubscriptionRepository repository;

    public WebhookSubscriptionServiceImpl(WebhookSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveSubscription(WebhookSubscriptionDto dto) {
        WebhookSubscriptionEntity entity = WebhookSubscriptionEntity.builder()
                .id(WebhookSubscriptionId.builder()
                        .streamerInternalId(dto.getStreamerInternalId())
                        .subscriptionType(dto.getSubscriptionType())
                        .build())
                .streamerProviderId(dto.getStreamerProviderId())
                .providerName(dto.getProviderName())
                .subscriptionStatus(dto.getSubscriptionStatus())
                .subscriptionId(dto.getSubscriptionId())
                .retryCount(dto.getRetryCount())
                .build();

        repository.save(entity);
    }

    @Override
    public List<WebhookSubscriptionEntity> getAllUncreatedSubscriptions() {
        return repository.getAllUncreatedSubscriptions();
    }

    @Override
    public void updateStatus(WebhookSubscriptionId id, WebhookSubscriptionStatus status) {
        repository.updateStatus(id, status.name());
    }

    @Override
    public void saveProviderSubscriptionId(WebhookSubscriptionId id, UUID providerSubscriptionId) {
        repository.saveProviderSubscriptionId(id, providerSubscriptionId);
    }

    @Override
    public List<WebhookSubscriptionEntity> getAllSubscriptionsByStatus(WebhookSubscriptionStatus status) {
        return repository.getAllSubscriptionsByStatus(status.name());
    }

    @Override
    public void resetSubscription(WebhookSubscriptionId id) {
        repository.incrementRetryCountAndResetSubscription(id);
    }

    @Override
    public void deleteSubscription(WebhookSubscriptionId subscriptionId) {
        repository.deleteById(subscriptionId);
    }

    @Override
    public void incrementRetryCount(WebhookSubscriptionId subscriptionId) {
        repository.incrementRetryCount(subscriptionId);
    }
}
