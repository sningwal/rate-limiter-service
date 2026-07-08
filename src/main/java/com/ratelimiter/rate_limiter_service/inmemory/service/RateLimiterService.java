package com.ratelimiter.rate_limiter_service.inmemory.service;

import com.ratelimiter.rate_limiter_service.inmemory.config.RateLimiterStoreConfig;
import com.ratelimiter.rate_limiter_service.inmemory.dto.RateLimitResult;
import com.ratelimiter.rate_limiter_service.inmemory.enums.RateLimiterType;
import com.ratelimiter.rate_limiter_service.inmemory.manager.RateLimiterManager;
import com.ratelimiter.rate_limiter_service.inmemory.ratelimiter.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {
    private final RateLimiterManager rateLimiterManager;
    private final RateLimiterStoreConfig rateLimiterConfigStore;
    public RateLimiterService(RateLimiterManager rateLimiterManager, RateLimiterStoreConfig rateLimiterConfigStore) {
        this.rateLimiterManager = rateLimiterManager;
        this.rateLimiterConfigStore = rateLimiterConfigStore;
    }
    public RateLimitResult checkRequest(
            String clientId,
            String rateLimiterType,
            HttpServletRequest request) {

        String rateLimiterTypeUpperCase = rateLimiterType.toUpperCase();

        String clientKey = resolveClientKey(clientId, request);

        RateLimiter rateLimiter = rateLimiterManager.get(RateLimiterType.valueOf(rateLimiterTypeUpperCase));
        boolean allowed = rateLimiter.allowRequest(clientKey, rateLimiterConfigStore.getConfig(RateLimiterType.valueOf(rateLimiterTypeUpperCase)));

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



