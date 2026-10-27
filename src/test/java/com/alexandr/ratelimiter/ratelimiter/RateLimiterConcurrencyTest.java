package com.alexandr.ratelimiter.ratelimiter;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterConcurrencyTest {

    private void assertNeverExceedsLimitUnderConcurrency(RateLimiter limiter, int maxRequests, int threadCount) throws InterruptedException {
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    AtomicInteger acceptedCount = new AtomicInteger(0);

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                startLatch.await();
                if (limiter.allowRequest("concurrent-client")) {
                    acceptedCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });
    }

    startLatch.countDown();
    doneLatch.await();
    executor.shutdown();

    assertEquals(maxRequests, acceptedCount.get(),
        "Expected exactly " + maxRequests + " accepted requests, but got " + acceptedCount.get() +
        " - a mismatch indicates a race condition in the limiter's synchronization");
    
    }

    @Test
    void fixedWindowRejectsExcessUnderConcurrency() throws InterruptedException {
        
        MutableClock clock = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(10, Duration.ofSeconds(2), clock);
        assertNeverExceedsLimitUnderConcurrency(limiter, 10, 50);
    
    }

    @Test
    void slidingWindowLogRejectsExcessUnderConcurrency() throws InterruptedException {
        
        MutableClock clock = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        SlidingWindowLogRateLimiter limiter = new SlidingWindowLogRateLimiter(10, Duration.ofSeconds(2), clock);
        assertNeverExceedsLimitUnderConcurrency(limiter, 10, 50);
    
    }

    @Test
    void slidingWindowCounterRejectsExcessUnderConcurrency() throws InterruptedException {
      
        MutableClock clock = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(10, Duration.ofSeconds(2), clock);
        assertNeverExceedsLimitUnderConcurrency(limiter, 10, 50);
    
    }

    @Test
    void tokenBucketRejectsExcessUnderConcurrency() throws InterruptedException {
       
        MutableClock clock = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 5, clock);
        assertNeverExceedsLimitUnderConcurrency(limiter, 10, 50);
    
    }



}