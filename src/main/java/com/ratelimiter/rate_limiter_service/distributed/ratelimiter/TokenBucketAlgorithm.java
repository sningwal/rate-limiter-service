package com.ratelimiter.rate_limiter_service.distributed.ratelimiter;

import com.ratelimiter.rate_limiter_service.distributed.config.AlgorithmConfig;
import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TokenBucketAlgorithm extends RateLimiter {

    private final RedisTemplate<String, Object> redis;
    private final DefaultRedisScript<Long> script;

    public TokenBucketAlgorithm(@Qualifier("redisTemplate") RedisTemplate<String, Object> redis, @Qualifier("tokenBucketScript") DefaultRedisScript<Long> tokenBucketScript) {
        this.redis = redis;
        this.script = tokenBucketScript;
    }

    @Override
    public boolean allowRequest(String clientKey, AlgorithmConfig algorithmConfig) {
        double now = System.currentTimeMillis() / 1000.0;
        double refillRate = (double) algorithmConfig.getMaxRequests() / algorithmConfig.getWindowInSeconds();
        int ttl =algorithmConfig.getMaxRequests() / algorithmConfig.getWindowInSeconds() + 1;

        Long result = redis.execute(script, List.of(clientKey),
                String.valueOf( algorithmConfig.getMaxRequests()), String.valueOf(refillRate), String.valueOf(now), String.valueOf(ttl));

        return result != null && result == 1L;
    }


    @Override
    public RateLimiterType name() {
        return RateLimiterType.TOKEN_BUCKET;
    }
}
