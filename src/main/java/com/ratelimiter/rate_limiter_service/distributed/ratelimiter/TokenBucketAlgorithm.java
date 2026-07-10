package com.ratelimiter.rate_limiter_service.distributed.ratelimiter;

import com.ratelimiter.rate_limiter_service.distributed.config.AlgorithmConfig;
import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class TokenBucketAlgorithm extends RateLimiter {

    private final RedisTemplate<String, AlgorithmConfig> redis;
    private final DefaultRedisScript<Long> script;

    public TokenBucketAlgorithm(@Qualifier("redisTemplate") RedisTemplate<String, AlgorithmConfig> redis, @Qualifier("tokenBucketScript") DefaultRedisScript<Long> tokenBucketScript) {
        this.redis = redis;
        this.script = tokenBucketScript;
    }

    @Override
    public boolean allowRequest(String clientKey, AlgorithmConfig algorithmConfig) {
        int maxRequests = algorithmConfig.getMaxRequests();
        int windowInSeconds = algorithmConfig.getWindowInSeconds();
        if (maxRequests <= 0 || windowInSeconds <= 0) {
            throw new IllegalStateException(
                    "algorithmConfig not properly initialized for clientKey=" + clientKey +
                            ": maxRequests=" + maxRequests + ", windowInSeconds=" + windowInSeconds);
        }
        double refillRate = (double) maxRequests / windowInSeconds;

        Long result = redis.execute(
                script,
                List.of(clientKey),
                algorithmConfig.getMaxRequests(),
                refillRate,
                System.currentTimeMillis() / 1000.0,
               algorithmConfig.getWindowInSeconds()
        );
        return result != null && result == 1L;
    }


    @Override
    public RateLimiterType name() {
        return RateLimiterType.TOKEN_BUCKET;
    }
}
