package com.alexandr.ratelimiter.ratelimiter;

import java.time.*;
import java.util.concurrent.ConcurrentHashMap;


public class TokenBucketRateLimiter implements RateLimiter {

    private final double capacity;
    private final double refillRatePerSecond;
    private final Clock clock;
    private final ConcurrentHashMap<String, BucketState> clientMap;

    private static class BucketState {
        double tokens;
        Instant lastRefillTime;
        
    
    private BucketState(double tokens, Instant lastRefillTime){
        this.tokens = tokens;
        this.lastRefillTime = lastRefillTime;
    }
    }

    public TokenBucketRateLimiter (double capacity, double refillRatePerSecond, Clock clock){
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.clock = clock;
        this.clientMap = new ConcurrentHashMap<>();

    }

    @Override 
    public boolean allowRequest(String clientKey){
        BucketState state = clientMap.computeIfAbsent(clientKey, k -> new BucketState(capacity, clock.instant()));

        synchronized(state){
            Instant now = clock.instant();
            Duration elapsed = Duration.between(state.lastRefillTime, now);
            double elapsedSeconds = elapsed.toNanos() / 1000000000.0;

            state.tokens = Math.min(capacity, state.tokens + elapsedSeconds * refillRatePerSecond);
            state.lastRefillTime = now;

            if(state.tokens >= 1) {
                state.tokens --;
                return true;
            } else return false;
            
        }

    }

}
