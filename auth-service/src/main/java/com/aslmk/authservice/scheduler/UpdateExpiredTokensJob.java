package com.aslmk.authservice.scheduler;

import com.aslmk.authservice.service.TokenUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UpdateExpiredTokensJob {
    private final TokenUpdateService tokenUpdater;

    public UpdateExpiredTokensJob(TokenUpdateService tokenUpdater) {
        this.tokenUpdater = tokenUpdater;
    }

    @Scheduled(fixedRate = 3_600_000) // 1 hour
    public void updateExpiredTokens() {
        log.info("Updating expired tokens...");
        tokenUpdater.updateExpiredTokens();
    }
}
