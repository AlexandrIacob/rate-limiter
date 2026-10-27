package com.alexandr.ratelimiter.config;
import java.time.Clock;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alexandr.ratelimiter.ratelimiter.FixedWindowRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.NoOpRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.RateLimiter;
import com.alexandr.ratelimiter.ratelimiter.SlidingWindowCounterRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.SlidingWindowLogRateLimiter;
import com.alexandr.ratelimiter.ratelimiter.TokenBucketRateLimiter;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiter rateLimiter(RateLimiterProperties properties) {
        Duration window = Duration.ofSeconds(properties.getWindowSeconds());
        Clock clock = Clock.systemUTC();

        switch (properties.getAlgorithm()) {
            case "fixed-window":
                return new FixedWindowRateLimiter(properties.getMaxRequests(), window, clock);
            case "sliding-window-log":
                return new SlidingWindowLogRateLimiter(properties.getMaxRequests(), window, clock);
            case "sliding-window-counter":
                return new SlidingWindowCounterRateLimiter(properties.getMaxRequests(), window, clock);
            case "token-bucket":
                return new TokenBucketRateLimiter(properties.getMaxRequests(), properties.getRefillRatePerSecond(), clock);
            case "none":
                return new NoOpRateLimiter();
            default:
                throw new IllegalArgumentException("Unknown ratelimiter.algorithm: " + properties.getAlgorithm());
        }
    }
}