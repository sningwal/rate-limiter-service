package com.ratelimiter.rate_limiter_service.distributed.ratelimiter;

import com.ratelimiter.rate_limiter_service.distributed.config.AlgorithmConfig;
import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public abstract class RateLimiter {
        public abstract boolean allowRequest(String clientKey, AlgorithmConfig algorithmConfig);
        public abstract RateLimiterType name();
}
