package com.ratelimiter.rate_limiter_service.distributed.manager;

import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.distributed.ratelimiter.RateLimiter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class RedisRateLimiterManager {

    private final Map<RateLimiterType, RateLimiter> limiters;

    public RedisRateLimiterManager(
            List<RateLimiter> rateLimiters
    ) {

        this.limiters =
                rateLimiters.stream()
                        .collect(Collectors.toMap(
                                RateLimiter::name,
                                limiter -> limiter
                        ));
    }

    public RateLimiter get(RateLimiterType type) {
        return limiters.get(type);
    }
}