package com.alexandr.ratelimiter.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ratelimiter")
public class RateLimiterProperties {

    private String algorithm;
    private int maxRequests;
    private int windowSeconds;
    private double refillRatePerSecond;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public double getRefillRatePerSecond() {
        return refillRatePerSecond;
    }

    public void setRefillRatePerSecond(double refillRatePerSecond) {
        this.refillRatePerSecond = refillRatePerSecond;
    }
}