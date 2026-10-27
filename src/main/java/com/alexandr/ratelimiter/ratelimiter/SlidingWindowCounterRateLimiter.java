package com.alexandr.ratelimiter.ratelimiter;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowCounterRateLimiter implements RateLimiter {
    private final int maxRequests;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, WindowCounterState> clientState;

    private static class WindowCounterState {
        private int previousCount = 0;
        private int currentCount = 0;
        private Instant currentWindowStart;
    
        private WindowCounterState(Instant currentWindowStart){
        this.currentWindowStart = currentWindowStart;
    }
    }

    public SlidingWindowCounterRateLimiter(int maxRequests, Duration windowDuration, Clock clock){
        this.maxRequests = maxRequests;
        this.windowDuration = windowDuration;
        this.clock = clock;
        this.clientState = new ConcurrentHashMap<>();

    }

    private void rolloverIfNeeded(WindowCounterState state, Instant now){
        Duration elapsed = Duration.between(state.currentWindowStart, now);
        
        if(elapsed.compareTo(windowDuration.multipliedBy(2)) >= 0){

            state.previousCount = 0;
            state.currentCount = 0;
            state.currentWindowStart = now;

        } else if (elapsed.compareTo(windowDuration) >= 0){
            
            state.previousCount = state.currentCount;
            state.currentCount = 0;
            state.currentWindowStart = state.currentWindowStart.plus(windowDuration);
        
        }
    }

    @Override 
    public boolean allowRequest(String clientKey){
        WindowCounterState state = clientState.computeIfAbsent(clientKey, k-> new WindowCounterState(clock.instant()));

        synchronized(state){
            Instant now = clock.instant();
            rolloverIfNeeded(state, now);
            Duration elapsed = Duration.between(state.currentWindowStart, now);
            double overlapFraction = 1.0 - (double)elapsed.toNanos()/windowDuration.toNanos();
            double estimatedCount = state.currentCount + state.previousCount * overlapFraction;

            if(estimatedCount < maxRequests){
                state.currentCount ++;
                return true;
            } else return false;
        }
        
    }


}
