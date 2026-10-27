package com.alexandr.ratelimiter.benchmark;

import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.Options;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
        .include(RateLimiterBenchmark.class.getSimpleName())
        .warmupIterations(5)
        .measurementIterations(10)
        .forks(0)
        .build();

        new Runner(options).run();
    }
}