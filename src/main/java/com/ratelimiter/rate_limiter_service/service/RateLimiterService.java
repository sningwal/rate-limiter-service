package com.ratelimiter.rate_limiter_service.service;

import com.ratelimiter.rate_limiter_service.dto.RateLimitResult;
import com.ratelimiter.rate_limiter_service.manager.RateLimiterManager;
import com.ratelimiter.rate_limiter_service.ratelimiter.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@Service
public class RateLimiterService {
    private final RateLimiterManager rateLimiterManager;
    public RateLimiterService(RateLimiterManager rateLimiterManager) {
        this.rateLimiterManager = rateLimiterManager;
        System.out.println(rateLimiterManager.getRateLimiter());
    }
    public RateLimitResult checkRequest(
            String clientId,
            HttpServletRequest request) {

        String clientKey = resolveClientKey(clientId, request);

        boolean allowed = rateLimiterManager.allowRequest(clientKey);

        return new RateLimitResult(
                allowed,
                rateLimiterManager.getRateLimiter().name(),
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



