package com.aslmk.subscriptionservice.service;

import com.aslmk.subscriptionservice.domain.UserSubscriptionEntity;
import com.aslmk.subscriptionservice.domain.UserSubscriptionId;
import com.aslmk.subscriptionservice.dto.CreateUserSubscription;
import com.aslmk.subscriptionservice.dto.UserSubscriptionDto;
import com.aslmk.subscriptionservice.dto.UserSubscriptionsResponse;
import com.aslmk.subscriptionservice.repository.UserSubscriptionsRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionsRepository repository;

    public UserSubscriptionServiceImpl(UserSubscriptionsRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserSubscriptionsResponse getAllUserSubscriptions(UUID userId) {
        List<UserSubscriptionEntity> userSubscriptions = repository.findAllById_UserId(userId);

        return userSubscriptions.stream()
                .map(userSubscription ->
                        new UserSubscriptionDto(
                                userSubscription.getId().getStreamerId(),
                                userSubscription.getStreamerUsername(),
                                userSubscription.getStreamerProfileImageUrl(),
                                userSubscription.getProviderName()))
                .collect(Collectors
                        .collectingAndThen(Collectors.toList(), UserSubscriptionsResponse::new)
                );
    }

    @Override
    public boolean saveUserSubscription(CreateUserSubscription dto) {
        try {
            UserSubscriptionId id = UserSubscriptionId.builder()
                    .streamerId(dto.getStreamerId())
                    .userId(dto.getUserId())
                    .build();

            UserSubscriptionEntity userSubscription = UserSubscriptionEntity.builder()
                    .id(id)
                    .streamerUsername(dto.getStreamerUsername())
                    .streamerProfileImageUrl(dto.getStreamerProfileImageUrl())
                    .providerName(dto.getProviderName())
                    .build();

            repository.save(userSubscription);
            return true;
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public void deleteUserSubscription(UUID userId, UUID streamerId) {
        UserSubscriptionId subscriptionId = UserSubscriptionId.builder()
                .userId(userId)
                .streamerId(streamerId)
                .build();

        repository.deleteById(subscriptionId);
    }
}
