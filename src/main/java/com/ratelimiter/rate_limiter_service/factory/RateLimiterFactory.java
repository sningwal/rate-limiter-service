package com.ratelimiter.rate_limiter_service.factory;

import com.ratelimiter.rate_limiter_service.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.model.RateLimiterConfig;
import com.ratelimiter.rate_limiter_service.ratelimiter.RateLimiter;
import com.ratelimiter.rate_limiter_service.ratelimiter.SlidingWindowLogRateLimiter;
import com.ratelimiter.rate_limiter_service.ratelimiter.TokenBucketRateLimiter;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterFactory {
    public  RateLimiter createRateLimiter(RateLimiterType algo, RateLimiterConfig config) {
        return switch (algo) {
            case TOKEN_BUCKET -> new TokenBucketRateLimiter(config);
            case SLIDING_WINDOW_LOG -> new SlidingWindowLogRateLimiter(config);
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algo);
        };
    }
}