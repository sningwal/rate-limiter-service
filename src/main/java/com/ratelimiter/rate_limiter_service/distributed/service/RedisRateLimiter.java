package com.ratelimiter.rate_limiter_service.distributed.service;

import com.ratelimiter.rate_limiter_service.distributed.config.RedisRateLimiterStoreConfig;
import com.ratelimiter.rate_limiter_service.distributed.dto.RateLimitResult;
import com.ratelimiter.rate_limiter_service.distributed.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.distributed.manager.RedisRateLimiterManager;
import com.ratelimiter.rate_limiter_service.distributed.ratelimiter.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RedisRateLimiter {
    private final RedisRateLimiterManager rateLimiterManager;;
    private final RedisRateLimiterStoreConfig rateLimiterConfigStore;


    public RedisRateLimiter(RedisRateLimiterManager rateLimiterManager, RedisRateLimiterStoreConfig rateLimiterConfigStore) {
        this.rateLimiterManager = rateLimiterManager;
        this.rateLimiterConfigStore = rateLimiterConfigStore;
    }

    public RateLimitResult checkRequest(String clientId,
                                       String rateLimiterType,
                                       HttpServletRequest request) {

        String rateLimiterTypeUpperCase = rateLimiterType.replace('-','_').toUpperCase();

        String clientKey = resolveClientKey(clientId, request);

        RateLimiter rateLimiter = rateLimiterManager.get(RateLimiterType.valueOf(rateLimiterTypeUpperCase));
        boolean allowed =  rateLimiter.allowRequest(clientKey,rateLimiterConfigStore.getConfig(rateLimiter.name()));

        return new RateLimitResult(
                allowed,
                rateLimiter.name(),
                clientKey
        );
    }
    /**
     * If an X-Client-Id header is ever sent, it takes priority else IP-based.
     */
    private String resolveClientKey(String apiKey, HttpServletRequest request) {
        if (apiKey != null && !apiKey.isBlank()) {
            return "user:" + apiKey; // not exercised yet, no auth issuing keys
        }
        return "ip:" + extractClientIp(request);
    }
    /**
     * Prefers X-Forwarded-For (set by a reverse proxy like Nginx) since
     * request.getRemoteAddr() would otherwise return the proxy's own IP,
     * not the real client's. Falls back to getRemoteAddr() with no proxy.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}