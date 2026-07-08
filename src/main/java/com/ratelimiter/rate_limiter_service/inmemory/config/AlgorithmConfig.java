package com.ratelimiter.rate_limiter_service.inmemory.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlgorithmConfig {
    private int maxRequests;
    private int windowInSeconds;
}