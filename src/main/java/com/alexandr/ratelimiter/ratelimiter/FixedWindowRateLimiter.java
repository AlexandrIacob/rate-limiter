package com.alexandr.ratelimiter.ratelimiter;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter implements RateLimiter {
    
    private static class WindowState {
        Instant windowStart;
        int count;

    private WindowState(Instant windowStart, int count) {
        this.windowStart = windowStart;
        this.count = count;
    }
    }
    
    private final int maxRequests;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, WindowState> clientWindows;

    public FixedWindowRateLimiter(int maxRequests, Duration windowDuration, Clock clock) {
        this.maxRequests = maxRequests;
        this.windowDuration = windowDuration;
        this.clock = clock;
        this.clientWindows = new ConcurrentHashMap<>();
    }

    @Override
    public boolean allowRequest(String clientKey) {
        WindowState state = clientWindows.computeIfAbsent(clientKey, k -> new WindowState(clock.instant(), 0));
        synchronized (state) {
            Instant startInstant = clock.instant();
            Duration elapseDuration = Duration.between(state.windowStart, startInstant);
            if(elapseDuration.compareTo(windowDuration) >= 0) {
                state.windowStart = startInstant;
                state.count = 0;
            }
            if (state.count < maxRequests) {
                state.count++;
                return true;} else return false;

        }
    }
}
