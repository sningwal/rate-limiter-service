package com.ratelimiter.rate_limiter_service.distributed.config;

import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisRateLimiterStoreConfig {

    private final Map<RateLimiterType, AlgorithmConfig> configs = new ConcurrentHashMap<>();

    public RedisRateLimiterStoreConfig() {

        configs.put(
                RateLimiterType.TOKEN_BUCKET,
              new AlgorithmConfig(10, 60)
        );

//        configs.put(
//                RateLimiterType.FIXED_WINDOW,
//                new AlgorithmConfig(10, 60)
//        );
//
        configs.put(
                RateLimiterType.SLIDING_WINDOW_LOG,
                new AlgorithmConfig(10, 60)
        );
//
//        configs.put(
//                RateLimiterType.SLIDING_WINDOW_COUNTER,
//                new AlgorithmConfig(10, 60)
//        );
//
//        configs.put(
//                RateLimiterType.LEAKY_BUCKET,
//                new AlgorithmConfig(10, 60)
//        );
    }

    public AlgorithmConfig getConfig(RateLimiterType type) {
        return configs.get(type);
    }

    public void updateConfig(
            RateLimiterType type,
            AlgorithmConfig config
    ) {
        configs.put(type, config);
    }


    public Map<RateLimiterType, AlgorithmConfig> getAllConfigs() {
        return configs;
    }
}