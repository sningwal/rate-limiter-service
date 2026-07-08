package com.ratelimiter.rate_limiter_service.inmemory.ratelimiter;
import com.ratelimiter.rate_limiter_service.inmemory.config.AlgorithmConfig;
import com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBucketRateLimiter extends RateLimiter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>(); // clientKey - [Tokens lastRefillTimestampNanos]

    @Override
    public boolean allowRequest(String clientKey, AlgorithmConfig algorithmConfig) {
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> new Bucket(algorithmConfig.getMaxRequests()));
        return bucket.tryConsume((double) algorithmConfig.getMaxRequests() / algorithmConfig.getWindowInSeconds(),algorithmConfig.getMaxRequests());
    }

    @Override
    public RateLimiterType name() {
        return RateLimiterType.TOKEN_BUCKET;
    }
    /**
     * Holds mutable state per client. Access is synchronized per-bucket
     * (not globally) to keep contention low while still being correct
     * for concurrent requests from the same client.
     */
    private static class Bucket {
        private double tokens;
        private long lastRefillTimestampNanos;

        Bucket(long initialTokens) {
            this.tokens = initialTokens;
            this.lastRefillTimestampNanos = System.nanoTime();
        }
        synchronized boolean tryConsume(double refillRatePerSecond, long capacity) {
            refill(refillRatePerSecond, capacity);
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill(double refillRatePerSecond, long capacity) {
            long now = System.nanoTime();
            double secondsElapsed = (now - lastRefillTimestampNanos) / 1_000_000_000.0;
            double tokensToAdd = secondsElapsed * refillRatePerSecond;
            if (tokensToAdd > 0) {
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTimestampNanos = now;
            }
        }
    }
}
