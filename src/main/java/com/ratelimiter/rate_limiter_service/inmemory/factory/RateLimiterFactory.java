package com.ratelimiter.rate_limiter_service.inmemory.factory;

import com.ratelimiter.rate_limiter_service.inmemory.config.RateLimiterStoreConfig;
import com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.inmemory.ratelimiter.RateLimiter;
import com.ratelimiter.rate_limiter_service.inmemory.ratelimiter.SlidingWindowLogRateLimiter;
import com.ratelimiter.rate_limiter_service.inmemory.ratelimiter.TokenBucketRateLimiter;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterFactory {
    public RateLimiter createRateLimiter(RateLimiterType algo, RateLimiterStoreConfig rateLimiterConfigStore) {
        return switch (algo) {
            case TOKEN_BUCKET -> new TokenBucketRateLimiter();
            case SLIDING_WINDOW_LOG -> new SlidingWindowLogRateLimiter();
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algo);
        };
    }
}