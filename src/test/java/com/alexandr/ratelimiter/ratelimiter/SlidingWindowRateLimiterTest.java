package com.alexandr.ratelimiter.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SlidingWindowRateLimiterTest {
   
    @Test 
    void belowLimit(){
        MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        SlidingWindowRateLimiter x = new SlidingWindowRateLimiter(3, Duration.ofSeconds(2), a);
        for (int i = 0; i < 2; i++){
            assertTrue(x.allowRequest("Test1"));
        }
    }

    @Test
	void atLimit() {
		MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
		SlidingWindowRateLimiter x = new SlidingWindowRateLimiter(3, Duration.ofSeconds(2), a);
		for(int i = 0; i < 3; i++){
			assertTrue(x.allowRequest("Test2"));
		}
	}

	@Test
	void aboveLimit() {
		MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
		SlidingWindowRateLimiter x = new SlidingWindowRateLimiter(3, Duration.ofSeconds(2), a);	
		for(int i = 0; i < 4; i++){
			if (i <3 ) assertTrue(x.allowRequest("Test3"));
            else assertFalse(x.allowRequest("Test3"));
		}
	}

	@Test 
	void windowExpiry() {
		MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
		SlidingWindowRateLimiter x = new SlidingWindowRateLimiter(3, Duration.ofSeconds(2), a);
		for(int i = 0; i < 4; i++){
			x.allowRequest("Test4"); 
		}
		a.advance(Duration.ofSeconds(3));
		assertTrue(x.allowRequest("Test4"));
	}

    @Test 
    void burstNearWindowBoundaryIsStillBlocked(){
        MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
        SlidingWindowRateLimiter x = new SlidingWindowRateLimiter(3, Duration.ofSeconds(2), a);
        for(int i = 0; i < 3; i++){
            x.allowRequest("Test5");
        }
        a.advance(Duration.ofSeconds(1));
        assertFalse(x.allowRequest("Test5"));
        

    }
}


