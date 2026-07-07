package com.ratelimiter.rate_limiter_service.controller;

import com.ratelimiter.rate_limiter_service.ratelimiter.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class RateLimiterController {

    private final RateLimiter rateLimiter;
    public RateLimiterController(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * Sample protected resource, rate limited by client IP.
     * IP is the right primary key for now since this endpoint has no
     * auth in front of it — login, signup, health checks, etc. don't
     * have a user identity yet, so IP is the only thing to key on.
     * TODO (once auth exists): resolve an API key / authenticated user id
     * first, and only fall back to IP for anonymous callers. Keying by
     * user allows accurate, tiered limits (free vs paid) instead of one
     * shared limit per network. See resolveClientKey() below.
     */
    @GetMapping("/api/resource")
    public ResponseEntity<?> getResource(
            @RequestHeader(value = "X-Client-Id", required = false) String clientId,
            HttpServletRequest request) {

        String clientKey = resolveClientKey(clientId, request);

        if (!rateLimiter.allowRequest(clientKey)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "rate_limit_exceeded",
                            "algorithm",rateLimiter.name(),
                            "clientKey", clientKey
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "request allowed",
                "algorithm", rateLimiter.name(),
                "clientKey", clientKey
        ));
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