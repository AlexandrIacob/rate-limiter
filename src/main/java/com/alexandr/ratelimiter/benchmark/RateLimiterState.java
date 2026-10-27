package com.alexandr.ratelimiter.benchmark;
import java.time.Clock;
import java.time.Duration;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import com.alexandr.ratelimiter.ratelimiter.FixedWindowRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.NoOpRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.RateLimiter;
import com.alexandr.ratelimiter.ratelimiter.SlidingWindowCounterRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.SlidingWindowLogRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.TokenBucketRateLimiter;

@State(Scope.Thread)
public class RateLimiterState {

    @Param({"fixed-window", "sliding-window-log", "sliding-window-counter", "token-bucket", "none"})
    public String algorithm;

    public RateLimiter limiter;

    @Setup(Level.Trial)
    public void setup() {
        switch(this.algorithm){
            case "fixed-window":
                limiter = new FixedWindowRateLimiter(1000000, Duration.ofSeconds(3600), Clock.systemUTC());
                break; 
            case "sliding-window-log":
                limiter = new SlidingWindowLogRateLimiter(1000000, Duration.ofSeconds(3600), Clock.systemUTC());
                break;
            case "sliding-window-counter":
                limiter = new SlidingWindowCounterRateLimiter(1000000, Duration.ofSeconds(3600), Clock.systemUTC());
                break;
            case "token-bucket":
                limiter = new TokenBucketRateLimiter(1000000, 1000.0, Clock.systemUTC());
                break;
            case "none":
                limiter = new NoOpRateLimiter();
                break;
            default:
                throw new IllegalArgumentException("Unknown ratelimiter.algorithm: " + algorithm);

            }
        
    }
}