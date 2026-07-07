package com.ratelimiter.rate_limiter_service.ratelimiter;

import com.ratelimiter.rate_limiter_service.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.model.RateLimiterConfig;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public abstract class RateLimiter {
    protected final RateLimiterConfig rateLimiterConfig;
    protected final RateLimiterType rateLimiterType;
    public abstract boolean allowRequest(String clientKey);
    public abstract RateLimiterType name();

}
