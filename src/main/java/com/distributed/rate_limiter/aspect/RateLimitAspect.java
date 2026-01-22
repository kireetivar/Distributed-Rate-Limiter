package com.distributed.rate_limiter.aspect;

import com.distributed.rate_limiter.annotations.RateLimit;
import com.distributed.rate_limiter.exceptions.RateLimitExceededException;
import com.distributed.rate_limiter.limiters.TokenBucketRateLimiter;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
@Slf4j
public class RateLimitAspect {

    @Autowired
    StringRedisTemplate template;

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limiter.lua")));
        redisScript.setResultType(Long.class);
    }

    @Around("@annotation(rateLimitAnnotation)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimitAnnotation) throws Throwable {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        String ipAddress = httpServletRequest != null ? httpServletRequest.getRemoteAddr() : "";
        String endpoint = joinPoint.getSignature().toString();

        String key = "rate_limit:" + endpoint + ":" + ipAddress;
        List<String> keys = Collections.singletonList(key);

        String capacity = String.valueOf(rateLimitAnnotation.capacity());
        String refillRate = String.valueOf(rateLimitAnnotation.refillRate());
        String requestedTokens = "1";

        Long isAllowed = 0L;

        try{
            isAllowed = template.execute(redisScript, keys, capacity, refillRate, requestedTokens);
        } catch (Exception e) {
            log.error("Redis Rate Limiter is down! Allowing request through. Error: {}", e.getMessage());
            isAllowed = 1L;
        }

        if (isAllowed == null || isAllowed == 0) {
            log.warn("Too many requests from IP {} for endpoint {}", ipAddress, endpoint);
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

