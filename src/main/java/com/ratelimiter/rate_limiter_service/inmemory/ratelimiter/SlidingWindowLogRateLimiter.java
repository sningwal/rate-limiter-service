package com.ratelimiter.rate_limiter_service.inmemory.ratelimiter;

import com.ratelimiter.rate_limiter_service.inmemory.config.AlgorithmConfig;
import com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType.SLIDING_WINDOW_LOG;


@Component
public class SlidingWindowLogRateLimiter extends RateLimiter {

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientKey, AlgorithmConfig algorithmConfig) {
        long now  = System.nanoTime();
        Deque<Long> log = requestLog.computeIfAbsent(clientKey,k-> new ArrayDeque<>());

        long windowNanos =
                algorithmConfig.getWindowInSeconds() * 1_000_000_000L;

        synchronized (log) {
            while (!log.isEmpty() && now - log.peekFirst() >= windowNanos) {
                log.pollFirst();
            }
            if (log.size() >= algorithmConfig.getMaxRequests()) {
                return false;
            }
            log.addLast(now);
            return true;
        }

    }

    @Override
    public RateLimiterType name() {
        return SLIDING_WINDOW_LOG;
    }
}
