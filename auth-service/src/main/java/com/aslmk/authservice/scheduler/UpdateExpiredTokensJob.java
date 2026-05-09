package com.aslmk.authservice.scheduler;

import com.aslmk.authservice.service.token.TokenUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UpdateExpiredTokensJob {
    private final TokenUpdateService tokenUpdater;

    public UpdateExpiredTokensJob(TokenUpdateService tokenUpdater) {
        this.tokenUpdater = tokenUpdater;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void updateExpiredTokens() {
        log.debug("Updating expired tokens...");
        tokenUpdater.updateExpiredTokens();
    }
}
