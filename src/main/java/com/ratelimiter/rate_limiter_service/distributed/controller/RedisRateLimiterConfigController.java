package com.ratelimiter.rate_limiter_service.distributed.controller;

import com.ratelimiter.rate_limiter_service.distributed.config.AlgorithmConfig;
import com.ratelimiter.rate_limiter_service.distributed.config.RedisRateLimiterStoreConfig;
import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/redis/config")
public class RedisRateLimiterConfigController {

    private final RedisRateLimiterStoreConfig configStore;

    public RedisRateLimiterConfigController(RedisRateLimiterStoreConfig configStore) {
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
        RateLimiterType rateLimiterTypeEnum = Enum.valueOf(RateLimiterType.class, rateLimiterType.toUpperCase());
        configStore.updateConfig(rateLimiterTypeEnum, config);
        return "Updated " + rateLimiterType;
    }
}