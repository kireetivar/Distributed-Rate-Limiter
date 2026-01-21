package com.distributed.rate_limiter.config;

import com.distributed.rate_limiter.limiters.TokenBucketRateLimiter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

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

    @Bean
    public Cache<String, TokenBucketRateLimiter> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }
}
