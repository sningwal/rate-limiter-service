package com.ratelimiter.rate_limiter_service.manager;

import com.ratelimiter.rate_limiter_service.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.factory.RateLimiterFactory;
import com.ratelimiter.rate_limiter_service.model.RateLimiterConfig;
import com.ratelimiter.rate_limiter_service.ratelimiter.RateLimiter;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RateLimiterManager {

    private volatile RateLimiter rateLimiter;

    public RateLimiterManager(RateLimiterFactory factory) {

        RateLimiterConfig config =
                new RateLimiterConfig(
                        RateLimiterType.TOKEN_BUCKET,
                        10,
                        30
                );

        this.rateLimiter =
                factory.createRateLimiter(
                        config.getAlgorithm(),
                        config
                );
    }

    public boolean allowRequest(String clientKey) {
        return rateLimiter.allowRequest(clientKey);
    }

    public synchronized void updateRateLimiter(RateLimiter newRateLimiter) {
        this.rateLimiter = newRateLimiter;
    }
}