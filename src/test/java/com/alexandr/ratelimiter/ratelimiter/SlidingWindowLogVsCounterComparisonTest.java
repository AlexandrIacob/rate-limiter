package com.alexandr.ratelimiter.ratelimiter;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SlidingWindowLogVsCounterComparisonTest {

    @Test
    void logAllowsFreshBurstButCounterRestrictsIt() {
        MutableClock sharedClock = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        SlidingWindowLogRateLimiter log = new SlidingWindowLogRateLimiter(3, Duration.ofSeconds(2), sharedClock);
        SlidingWindowCounterRateLimiter counter = new SlidingWindowCounterRateLimiter(3, Duration.ofSeconds(2), sharedClock);
        
        for (int i = 0; i < 3; i++) {
            assertTrue(log.allowRequest("Test1"));
        }

        for (int i = 0; i < 3; i++) {
            assertTrue(counter.allowRequest("Test1"));
        }

        sharedClock.advance(Duration.ofMillis(2050));
        assertTrue(log.allowRequest("Test1"));
        assertTrue(counter.allowRequest("Test1"));

        assertTrue(log.allowRequest("Test1"));
        assertFalse(counter.allowRequest("Test1"));
    }
}
