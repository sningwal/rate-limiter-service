package com.ratelimiter.rate_limiter_service.distributed.config;

import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RedisRateLimiterStoreConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String REDIS_KEY = "rate_limiter:configs";

    private final RedisTemplate<String, AlgorithmConfig> redisTemplate;

    public RedisRateLimiterStoreConfig(
            RedisTemplate<String, AlgorithmConfig> redisTemplate
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
        Map<Object, Object> configs = redisTemplate.opsForHash().entries(REDIS_KEY);
        return (AlgorithmConfig) configs.get(type.name());
    }

    public void updateConfig(RateLimiterType type, AlgorithmConfig config) {
        String json = objectMapper.writeValueAsString(config);
        redisTemplate.opsForHash()
                .put(REDIS_KEY, type.name(), json);
    }

    public Map<RateLimiterType, AlgorithmConfig> getAllConfigs() {

        Map<Object, Object> configs =
                redisTemplate.opsForHash()
                        .entries(REDIS_KEY);

        return configs.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> RateLimiterType.valueOf(entry.getKey().toString()),
                        entry -> objectMapper.convertValue(
                                entry.getValue(),
                                AlgorithmConfig.class
                        )
                ));
    }
}