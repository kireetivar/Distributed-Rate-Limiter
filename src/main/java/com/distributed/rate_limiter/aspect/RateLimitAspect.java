package com.distributed.rate_limiter.aspect;

import com.distributed.rate_limiter.annotations.RateLimit;
import com.distributed.rate_limiter.exceptions.RateLimitExceededException;
import com.distributed.rate_limiter.limiters.TokenBucketRateLimiter;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
@Slf4j
public class RateLimitAspect {

    @Autowired
    Cache<String, TokenBucketRateLimiter> cache;

    @Around("@annotation(rateLimitAnnotation)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimitAnnotation) throws Throwable {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        String ipAddress = httpServletRequest != null ? httpServletRequest.getRemoteAddr() : "";
        String endpoint = joinPoint.getSignature().toString();
        String signature = endpoint + "|" + ipAddress;
        var rateLimiter = cache.get(signature, k -> new TokenBucketRateLimiter(rateLimitAnnotation.capacity(), rateLimitAnnotation.refillRate()));
        if (!rateLimiter.tryAcquire()) {
            log.warn("Too many requests from IP {} for endpoint {} ", ipAddress, endpoint);
            throw new RateLimitExceededException("Too Many Requests");
        }
        return joinPoint.proceed();
    }

    private HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        log.debug("Not called in the context of an HTTP request");
        return null;
    }
}

