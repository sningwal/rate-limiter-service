package com.ratelimiter.rate_limiter_service.dto;

import com.ratelimiter.rate_limiter_service.enums.RateLimiterType;

public record RateLimitResult(
        boolean allowed,
        RateLimiterType algorithm,
        String clientKey
) {}
