package com.ratelimiter.rate_limiter_service.inmemory.dto;


import com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType;

public record RateLimitResult(
        boolean allowed,
        RateLimiterType algorithm,
        String clientKey
) {}
