package com.ratelimiter.rate_limiter_service.model;

import lombok.Getter;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "ratelimiter")
public class RateLimiterConfig {
    private volatile int maxRequests = 10;
    private volatile int windowInSeconds = 60;
    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }
    public void setWindowInSeconds(int windowInSeconds) {
        this.windowInSeconds = windowInSeconds;
    }
}