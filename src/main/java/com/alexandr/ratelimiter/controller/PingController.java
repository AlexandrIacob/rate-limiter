package com.alexandr.ratelimiter.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.alexandr.ratelimiter.ratelimiter.RateLimiter;

@RestController
public class PingController {

    private final RateLimiter rateLimiter;

    public PingController(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader(value = "X-Client-Id", defaultValue = "anonymous") String clientId) {
        if (rateLimiter.allowRequest(clientId)) {
            return ResponseEntity.ok("pong");
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}