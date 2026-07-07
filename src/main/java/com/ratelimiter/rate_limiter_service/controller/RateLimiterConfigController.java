package com.ratelimiter.rate_limiter_service.controller;

import com.ratelimiter.rate_limiter_service.model.RateLimiterConfig;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class RateLimiterConfigController {

    private final RateLimiterConfig config;

    public RateLimiterConfigController(RateLimiterConfig config) {
        this.config = config;
    }

    @PutMapping("/config")
    public RateLimiterConfig update(@RequestBody RateLimiterConfig newConfig) {
        config.setMaxRequests(newConfig.getMaxRequests());
        config.setWindowInSeconds(newConfig.getWindowInSeconds());
        return config;
    }

    @GetMapping("/config")
    public RateLimiterConfig get() {
        return config;
    }
}