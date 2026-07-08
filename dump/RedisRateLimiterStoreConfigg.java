package com.ratelimiter.rate_limiter_service.distributed.config;

import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RedisRateLimiterStoreConfigg {

    private static final String REDIS_KEY = "rate_limiter:configs";

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisRateLimiterStoreConfigg(
         RedisTemplate<String, Object> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;

        initializeDefaults();
    }

    private void initializeDefaults() {

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_KEY))) {

            updateConfig(
                    RateLimiterType.TOKEN_BUCKET,
                    new AlgorithmConfig(10, 60)
            );

            updateConfig(
                    RateLimiterType.SLIDING_WINDOW_LOG,
                    new AlgorithmConfig(10, 60)
            );
        }
    }


    public AlgorithmConfig getConfig(RateLimiterType type) {

        Map<Object, Object> configs =
                redisTemplate.opsForHash()
                        .entries(REDIS_KEY);

        return (AlgorithmConfig) configs.get(type.name());
    }


    public void updateConfig(
            RateLimiterType type,
            AlgorithmConfig config
    ) {

        redisTemplate.opsForHash()
                .put(
                        REDIS_KEY,
                        type.name(),
                        config
                );
    }


    public Map<RateLimiterType, AlgorithmConfig> getAllConfigs() {

        return redisTemplate.opsForHash()
                .entries(REDIS_KEY)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> RateLimiterType.valueOf((String)e.getKey()),
                        e -> (AlgorithmConfig)e.getValue()
                ));
    }
}