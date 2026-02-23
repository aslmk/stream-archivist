package com.aslmk.subscriptionservice.service.impl;

import com.aslmk.subscriptionservice.dto.CreateUserSubscription;
import com.aslmk.subscriptionservice.dto.UserSubscriptionDto;
import com.aslmk.subscriptionservice.dto.UserSubscriptionsResponse;
import com.aslmk.subscriptionservice.entity.UserSubscriptionEntity;
import com.aslmk.subscriptionservice.repository.UserSubscriptionsRepository;
import com.aslmk.subscriptionservice.service.UserSubscriptionService;
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
    public UserSubscriptionsResponse getAllUserSubscriptions(String userId) {
        UUID uuidUserId = UUID.fromString(userId);
        List<UserSubscriptionEntity> userSubscriptions = repository.findAllByUserId(uuidUserId);

        return userSubscriptions.stream()
                .map(userSubscription ->
                        new UserSubscriptionDto(
                                userSubscription.getStreamerId(),
                                userSubscription.getStreamerUsername(),
                                userSubscription.getStreamerProfileImageUrl(),
                                userSubscription.getProviderName()))
                .collect(Collectors
                        .collectingAndThen(Collectors.toList(), UserSubscriptionsResponse::new)
                );
    }

    @Override
    public UserSubscriptionEntity saveUserSubscription(CreateUserSubscription dto) {
        UserSubscriptionEntity userSubscription = UserSubscriptionEntity.builder()
                .streamerId(dto.getStreamerId())
                .streamerUsername(dto.getStreamerUsername())
                .streamerProfileImageUrl(dto.getStreamerProfileImageUrl())
                .providerName(dto.getProviderName())
                .userId(dto.getUserId())
                .build();

        return repository.save(userSubscription);
    }
}
