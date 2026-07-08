package com.ratelimiter.rate_limiter_service.inmemory.controller;

import com.ratelimiter.rate_limiter_service.inmemory.dto.RateLimitResult;
import com.ratelimiter.rate_limiter_service.inmemory.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("inMemoryRateLimiterController")
@RequestMapping("/api/v1/")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
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
    @GetMapping("test")
    public  ResponseEntity<?> getRe(){
        return  ResponseEntity.ok("test");
    }
    @GetMapping("/resource/{algo}")
    public ResponseEntity<?> getResource(
            @PathVariable("algo") String algorithm,
            @RequestHeader(value = "X-Client-Id", required = false) String clientId,
            HttpServletRequest request) {
        System.out.println("at resource...");
        RateLimitResult result = rateLimiterService.checkRequest(clientId,algorithm,request);


        if (!result.allowed()) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "rate_limit_exceeded",
                            "algorithm", result.algorithm()
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "request allowed",
                "algorithm", result.algorithm(),
                "clientKey",result.clientKey()
        ));
    }
}