package com.aslmk.authservice.scheduler;

import com.aslmk.authservice.service.token.TokenUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UpdateExpiredTokensJob {
    private final TokenUpdateService tokenUpdater;

    public UpdateExpiredTokensJob(TokenUpdateService tokenUpdater) {
        this.tokenUpdater = tokenUpdater;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void updateExpiredTokens() {
        tokenUpdater.updateExpiredTokens();
    }
}
