package com.ratelimiter.rate_limiter_service.distributed.controller;

import com.ratelimiter.rate_limiter_service.distributed.dto.RateLimitResult;
import com.ratelimiter.rate_limiter_service.distributed.service.RedisRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("distributedRateLimiterController")
@RequestMapping("api/v1/redis")
public class RateLimiterController {

    private final RedisRateLimiter rateLimiterService;

    public RateLimiterController(RedisRateLimiter rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/resource/{algo}")
    public ResponseEntity<?> getResource(
            @PathVariable("algo") String algorithm,
            @RequestHeader(value = "X-Client-Id", required = false) String clientId,
            HttpServletRequest request) {

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