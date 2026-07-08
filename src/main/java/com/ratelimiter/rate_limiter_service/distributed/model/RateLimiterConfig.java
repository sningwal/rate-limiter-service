package com.ratelimiter.rate_limiter_service.distributed.model;

import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RateLimiterConfig {
    private RateLimiterType algorithm;
    private int maxRequests;
    private int windowInSeconds;
}