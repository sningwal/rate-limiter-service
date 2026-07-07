package com.ratelimiter.rate_limiter_service.controller;

import com.ratelimiter.rate_limiter_service.factory.RateLimiterFactory;
import com.ratelimiter.rate_limiter_service.manager.RateLimiterManager;
import com.ratelimiter.rate_limiter_service.model.RateLimiterConfig;
import com.ratelimiter.rate_limiter_service.ratelimiter.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RateLimiterConfigController {

    private final RateLimiterManager rateLimiterManager;
    private final RateLimiterFactory rateLimiterFactory;

    public RateLimiterConfigController(
            RateLimiterManager rateLimiterManager,
            RateLimiterFactory rateLimiterFactory) {
        this.rateLimiterManager = rateLimiterManager;
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @PutMapping("/config")
    public ResponseEntity<?>  update(
            @RequestBody RateLimiterConfig newConfig) {

        RateLimiter limiter =
                rateLimiterFactory.createRateLimiter(
                        newConfig.getAlgorithm(),
                        newConfig
                );
        rateLimiterManager.updateRateLimiter(limiter);
        return ResponseEntity.ok("updated !.");
    }


    @GetMapping("/config")
    public RateLimiterConfig get() {
        RateLimiter rateLimiter = rateLimiterManager.getRateLimiter();

        return new RateLimiterConfig(
                rateLimiter.getRateLimiterConfig().getAlgorithm(),
                rateLimiter.getRateLimiterConfig().getMaxRequests(),
                rateLimiter.getRateLimiterConfig().getWindowInSeconds()
        );
    }
}