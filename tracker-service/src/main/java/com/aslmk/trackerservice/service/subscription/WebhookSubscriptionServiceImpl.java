package com.aslmk.trackerservice.service.subscription;

import com.aslmk.trackerservice.repository.WebhookSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class WebhookSubscriptionServiceImpl implements WebhookSubscriptionService {
    private final WebhookSubscriptionRepository repository;

    public WebhookSubscriptionServiceImpl(WebhookSubscriptionRepository repository) {
        this.repository = repository;
    }


}
