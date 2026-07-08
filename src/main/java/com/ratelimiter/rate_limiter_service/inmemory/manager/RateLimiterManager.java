package com.ratelimiter.rate_limiter_service.inmemory.manager;

import com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.inmemory.ratelimiter.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RateLimiterManager {

    private final Map<RateLimiterType, RateLimiter> limiters;

    public RateLimiterManager(List<RateLimiter> rateLimiters
    ) {
        this.limiters = rateLimiters.stream()
                .collect(Collectors.toMap(RateLimiter::name,rateLimiter ->rateLimiter ));
    }

    public RateLimiter get(RateLimiterType type) {
        return limiters.get(type);
    }
}