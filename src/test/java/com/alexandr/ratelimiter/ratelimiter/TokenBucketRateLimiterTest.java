package com.alexandr.ratelimiter.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TokenBucketRateLimiterTest {

    @Test 
    void burstUpToCapacityThenRejected(){
        MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        TokenBucketRateLimiter x = new TokenBucketRateLimiter(3, 1.0, a);

        for(int i = 0; i < 4; i++){
            if(i < 3) assertTrue(x.allowRequest("Test1"));
            else assertFalse(x.allowRequest("Test1"));
        }
    }

    @Test 
    void refillOverTime(){
        MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        TokenBucketRateLimiter x = new TokenBucketRateLimiter(3, 1.0, a);

        for(int i = 0; i < 5; i++){
            if(i >= 0 && i <3) assertTrue(x.allowRequest("Test2"));
            if (i == 3) {
            a.advance(Duration.ofSeconds(1));
             assertTrue(x.allowRequest("Test2"));
            } else if (i > 3) assertFalse(x.allowRequest("Test2"));
        }
    }

    @Test
    void refillCapedAtCapacity(){
        MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        TokenBucketRateLimiter x = new TokenBucketRateLimiter(3, 1.0, a);

        for(int i = 0; i < 7; i++){
            if(i < 3) assertTrue(x.allowRequest("Test3"));
            if(i == 3) a.advance(Duration.ofSeconds(32));
            if(i >2 && i < 6){
                assertTrue(x.allowRequest("Test3"));
            }
            if(i == 6) assertFalse(x.allowRequest("Test3"));
        }
    }

    @Test 
    void partialRefillReject(){
        MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        TokenBucketRateLimiter x = new TokenBucketRateLimiter(3, 1.0, a);

        for(int i = 0; i < 5; i++){
            if (i >= 0 && i <3) assertTrue(x.allowRequest("Test4"));
            if(i == 3) a.advance(Duration.ofMillis(500));
            if(i>2){
                assertFalse(x.allowRequest("Test4"));
            }
        }
    }
}
