package com.ratelimiter.rate_limiter_service.distributed.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlgorithmConfig implements Serializable {
    private int maxRequests;
    private int windowInSeconds;
}