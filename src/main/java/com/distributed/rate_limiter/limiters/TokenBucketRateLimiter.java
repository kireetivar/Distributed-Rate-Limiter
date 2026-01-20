package com.distributed.rate_limiter.limiters;

public class TokenBucketRateLimiter {

    private final long ONE_SECOND_NS = 1_000_000_000L;

    private final long capacity;
    private final double refillRate;

    private double currentTokens;
    private long lastRefillTime;

    public TokenBucketRateLimiter(long capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.currentTokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    public synchronized boolean tryAcquire() {
        long currentTime = System.nanoTime();
        long timeDifference = currentTime - lastRefillTime;
        currentTokens += (double) timeDifference/ (double) ONE_SECOND_NS * refillRate;
        currentTokens = currentTokens > capacity ? capacity : currentTokens;
        lastRefillTime = currentTime;

        if (currentTokens >= 1) {
            currentTokens--;
            return true;
        }
        return false;
    }
}
