package com.alexandr.ratelimiter.ratelimiter;

public class NoOpRateLimiter implements RateLimiter {

    @Override 
    public boolean allowRequest(String clientKey){
        return true;
    }
}
