package com.ratelimiter.rate_limiter_service.distributed.dto;


import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;

public record RateLimitResult(
        boolean allowed,
        RateLimiterType algorithm,
        String clientKey
) {}
