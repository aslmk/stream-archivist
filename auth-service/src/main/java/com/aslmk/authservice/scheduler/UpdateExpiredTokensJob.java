package com.aslmk.authservice.scheduler;

import com.aslmk.authservice.service.TokenUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UpdateExpiredTokensJob {
    private final TokenUpdateService tokenUpdater;

    public UpdateExpiredTokensJob(TokenUpdateService tokenUpdater) {
        this.tokenUpdater = tokenUpdater;
    }

    @Scheduled(fixedRate = 3_600_000) // 1 hour
    public void updateExpiredTokens() {
        tokenUpdater.updateExpiredTokens();
    }
}
