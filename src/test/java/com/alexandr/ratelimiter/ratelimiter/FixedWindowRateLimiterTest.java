package com.alexandr.ratelimiter.ratelimiter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.*;

import org.junit.jupiter.api.Test;

public class FixedWindowRateLimiterTest {

	@Test
	void belowLimit() {
		MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
		FixedWindowRateLimiter x = new FixedWindowRateLimiter(3, Duration.ofSeconds(2), a);
		for(int i = 0; i < 2; i++){
			assertTrue(x.allowRequest("Test1")); 
		}
	}

	@Test
	void atLimit() {
		MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
		FixedWindowRateLimiter x = new FixedWindowRateLimiter(3, Duration.ofSeconds(2), a);
		for(int i = 0; i < 3; i++){
			assertTrue(x.allowRequest("Test2"));
		}
	}

	@Test
	void aboveLimit() {
		MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
		FixedWindowRateLimiter x = new FixedWindowRateLimiter(3, Duration.ofSeconds(2), a);	
		for(int i = 0; i < 4; i++){
			if (i <3 ) assertTrue(x.allowRequest("Test3"));
            else assertFalse(x.allowRequest("Test3"));
		}
	}

	@Test 
	void windowExpiry() {
		MutableClock a = new MutableClock(ZoneId.of("UTC"), Instant.EPOCH);
		FixedWindowRateLimiter x = new FixedWindowRateLimiter(3, Duration.ofSeconds(2), a);
		for(int i = 0; i < 4; i++){
			x.allowRequest("Test4"); 
		}
		a.advance(Duration.ofSeconds(3));
		assertTrue(x.allowRequest("Test4"));
	}
}
