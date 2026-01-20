package com.distributed.rate_limiter.config;

import com.distributed.rate_limiter.limiters.TokenBucketRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    private final long capacity;
    private final double refillRate;

    public RateLimiterConfig(@Value("${ratelimiter.capacity:1}") long capacity,
                             @Value("${ratelimiter.refillRate:0.1}") double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    @Bean
    public TokenBucketRateLimiter tokenBucketRateLimiter() {
        return new TokenBucketRateLimiter(capacity, refillRate);
    }
}
