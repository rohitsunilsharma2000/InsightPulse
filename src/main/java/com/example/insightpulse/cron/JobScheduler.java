//package com.example.prometheus_grafana_dashboard.controller;
//
//
//
//
//import io.micrometer.core.instrument.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//public class JobScheduler {
//
//    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);
//    private final Random random = new Random();
//
//    private final Counter jobThroughput;
//    private final Counter jobFailures;
//    private final Timer jobExecutionDuration;
//    private final AtomicInteger jobAvailability;
//
//    public JobScheduler(MeterRegistry meterRegistry) {
//        this.jobThroughput = Counter.builder("job.throughput")
//                                    .description("Total number of jobs processed")
//                                    .register(meterRegistry);
//
//        this.jobFailures = Counter.builder("job.failures")
//                                  .description("Number of failed job executions")
//                                  .register(meterRegistry);
//
//        this.jobExecutionDuration = Timer.builder("job.execution.duration")
//                                         .description("Time taken to execute jobs")
//                                         .publishPercentileHistogram()
//                                         .register(meterRegistry);
//
//        this.jobAvailability = new AtomicInteger(1);  // Initialize with "available"
//
//        // Register the gauge using a supplier from the AtomicInteger
//        Gauge.builder("job.availability", jobAvailability, AtomicInteger::get)
//             .description("1 if job is available, 0 if unavailable")
//             .register(meterRegistry);
//    }
//
//    /**
//     * Cron-based scheduled job that runs every 2 seconds.
//     */
//    @Scheduled(cron = "*/1 * * * * *")
//    public void processJob() {
//        long start = System.currentTimeMillis();
//
//        try {
//            logger.info("Running scheduled job...");
//            System.out.println("Running scheduled job...");
//            // Simulate delay
//            Thread.sleep(random.nextInt(500));
//
//            // Simulate failure
//            if (random.nextDouble() < 0.2) {
//                jobFailures.increment();
//                jobAvailability.set(0);
//                logger.error("Job failed");
//                throw new RuntimeException("Simulated job failure");
//            }
//
//            logger.info("Job executed successfully");
//
//        } catch (Exception e) {
//            jobAvailability.set(0);
//        } finally {
//            long duration = System.currentTimeMillis() - start;
//            jobThroughput.increment();
//            jobExecutionDuration.record(duration, TimeUnit.MILLISECONDS);
//            jobAvailability.set(1);
//        }
//    }
//}
