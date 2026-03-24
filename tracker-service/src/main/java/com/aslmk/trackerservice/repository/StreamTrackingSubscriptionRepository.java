package com.aslmk.trackerservice.repository;

import com.aslmk.trackerservice.domain.StreamTrackingSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StreamTrackingSubscriptionRepository extends JpaRepository<StreamTrackingSubscriptionEntity, UUID> {
    List<StreamTrackingSubscriptionEntity> findAllByStreamerId(UUID streamerId);
}
