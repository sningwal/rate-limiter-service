package com.ratelimiter.rate_limiter_service.inmemory.controller;

import com.ratelimiter.rate_limiter_service.inmemory.config.AlgorithmConfig;
import com.ratelimiter.rate_limiter_service.inmemory.config.RateLimiterStoreConfig;
import com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class RateLimiterConfigController {

    private final RateLimiterStoreConfig configStore;

    public RateLimiterConfigController(
            RateLimiterStoreConfig configStore
    ) {
        this.configStore = configStore;
    }


    @GetMapping
    public Map<RateLimiterType, AlgorithmConfig> getAllConfigs() {

        return configStore.getAllConfigs();
    }

    @PutMapping("/{algorithm}")
    public String updateConfig(
            @PathVariable("algorithm") String rateLimiterType,
            @RequestBody AlgorithmConfig config
    ) {
        RateLimiterType rateLimiterTypeEnum = Enum.valueOf(RateLimiterType.class, rateLimiterType.replace('-','_').toUpperCase());
        configStore.updateConfig(rateLimiterTypeEnum, config);
        return "Updated " + rateLimiterType;
    }
}