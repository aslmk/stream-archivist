package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.domain.WebhookSubscriptionEntity;
import com.aslmk.trackerservice.domain.WebhookSubscriptionId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookSubscriptionRepository extends CrudRepository<WebhookSubscriptionEntity, WebhookSubscriptionId> {}
