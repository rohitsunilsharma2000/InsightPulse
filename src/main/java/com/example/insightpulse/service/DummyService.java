package com.example.insightpulse.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class DummyService {

    private final Counter jobThroughputCounter;
    private final Counter jobFailuresCounter;
    private final Timer jobExecutionTimer;

    public DummyService( MeterRegistry registry) {
        // Counter for job throughput
        this.jobThroughputCounter = Counter.builder("dummy_job_throughput_total")
                                           .description("Total dummy jobs executed")
                                           .register(registry);

        // Counter for job failures
        this.jobFailuresCounter = Counter.builder("dummy_job_failures_total")
                                         .description("Total dummy job failures")
                                         .register(registry);

        // Timer for job execution duration
        this.jobExecutionTimer = Timer.builder("dummy_job_execution_duration_seconds")
                                      .description("Duration of dummy job execution")
                                      .publishPercentileHistogram()
                                      .register(registry);
    }

    // Simulate job execution
    public void job_history_success() {
        long startTime = System.nanoTime();

        // Simulate job success or failure
        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
            jobFailuresCounter.increment(); // Simulate a failure
            throw new RuntimeException("Simulated job failure");
        }

        jobThroughputCounter.increment(); // Increment the job throughput counter

        // Simulate job duration
        try {
            long duration = ThreadLocalRandom.current().nextInt(200, 1001); // Random job duration between 200ms and 1000ms
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            long endTime = System.nanoTime();
            jobExecutionTimer.record(endTime - startTime, TimeUnit.NANOSECONDS); // Record execution time
        }
    }
}
