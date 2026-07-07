package com.ratelimiter.rate_limiter_service.ratelimiter;

import com.ratelimiter.rate_limiter_service.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.model.RateLimiterConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public abstract class RateLimiter {
    protected final RateLimiterConfig rateLimiterConfig;
    public abstract boolean allowRequest(String clientKey);
    public abstract RateLimiterType name();

}
