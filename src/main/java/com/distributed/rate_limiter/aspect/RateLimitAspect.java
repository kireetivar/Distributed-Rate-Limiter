package com.distributed.rate_limiter.aspect;

import com.distributed.rate_limiter.annotations.RateLimit;
import com.distributed.rate_limiter.exceptions.RateLimitExceededException;
import com.distributed.rate_limiter.limiters.TokenBucketRateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
public class RateLimitAspect {

    public final ConcurrentHashMap<String, TokenBucketRateLimiter> tokenBucket = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimitAnnotation)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimitAnnotation) throws Throwable {
        String signature = joinPoint.getSignature().toLongString();
        var rateLimiter = tokenBucket.computeIfAbsent(signature, k -> new TokenBucketRateLimiter(rateLimitAnnotation.capacity(), rateLimitAnnotation.refillRate()));
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitExceededException("Too Many Requests");
        }
        return joinPoint.proceed();
    }
}

