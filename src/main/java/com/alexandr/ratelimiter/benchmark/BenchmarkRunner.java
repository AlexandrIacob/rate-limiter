package com.alexandr.ratelimiter.benchmark;

import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.Options;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
        .include(RateLimiterBenchmark.class.getSimpleName())
        .warmupIterations(3)
        .measurementIterations(5)
        .forks(0)
        .build();

        new Runner(options).run();
    }
}