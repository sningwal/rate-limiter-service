package com.ratelimiter.rate_limiter_service.ratelimiter;
import com.ratelimiter.rate_limiter_service.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.model.RateLimiterConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowLogRateLimiter extends RateLimiter {

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiter(RateLimiterConfig rateLimiterConfig) {
        super(rateLimiterConfig);
    }

    @Override
    public boolean allowRequest(String clientKey) {

        long now  = System.nanoTime();
        Deque<Long> log = requestLog.computeIfAbsent(clientKey,k-> new ArrayDeque<>());

        long windowNanos =
                rateLimiterConfig.getWindowInSeconds() * 1_000_000_000L;

        synchronized (log) {
            while (!log.isEmpty() && now - log.peekFirst() >= windowNanos) {
                log.pollFirst();
            }
            if (log.size() >= rateLimiterConfig.getMaxRequests()) {
                return false;
            }
            log.addLast(now);
            return true;
        }
    }

    @Override
    public RateLimiterType name() {
        return RateLimiterType.SLIDING_WINDOW_LOG;
    }
}
