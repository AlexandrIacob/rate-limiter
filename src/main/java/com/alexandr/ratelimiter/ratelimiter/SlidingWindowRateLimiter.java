package com.alexandr.ratelimiter.ratelimiter;
import java.time.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowRateLimiter implements RateLimiter {
    
    private final int maxRequests;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> clientLogs; 

    public SlidingWindowRateLimiter(int maxRequests, Duration windowDuration, Clock clock){
        this.maxRequests = maxRequests;
        this.windowDuration = windowDuration;
        this.clock = clock;
        this.clientLogs = new ConcurrentHashMap<>();
    }

    @Override 
    public boolean allowRequest(String clientKey) {
        Deque<Instant> log = clientLogs.computeIfAbsent(clientKey, k -> new ArrayDeque<>());
        synchronized(log) {
            Instant now = clock.instant();
            Instant cutoff = now.minus(windowDuration);
            while (!log.isEmpty() && log.peekFirst().isBefore(cutoff)) {
                log.pollFirst();
            }
            
            if (log.size() < maxRequests) {
            log.addLast(now);
            return true;
            } else return false;

        }
        
    }
}
