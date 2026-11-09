package com.sagar.auth.scheduler;

import com.sagar.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    /**
     * Removes expired refresh tokens every day at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {

        refreshTokenService.deleteExpiredTokens();
    }
}