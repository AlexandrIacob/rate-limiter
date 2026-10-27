package com.alexandr.ratelimiter.benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;


public class RateLimiterBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public boolean allowRequest(RateLimiterState state) {
        return state.limiter.allowRequest("benchmark-client");
    }
}
