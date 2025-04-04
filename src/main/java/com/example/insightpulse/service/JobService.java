package com.example.insightpulse.service;


import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final Counter userRegistrationThroughput;
    private final Counter paymentFailures;
    private final Timer reportGenerationTimer;
    private final AtomicInteger inventoryAvailability;

    public JobService(MeterRegistry registry) {
        // Counter for user registrations (throughput)
        this.userRegistrationThroughput = Counter.builder("user_registration_throughput_total")
                                                 .description("Total number of user registrations")
                                                 .register(registry);

        // Counter for failed payments
        this.paymentFailures = Counter.builder("payment_failures_total")
                                      .description("Number of failed payment attempts")
                                      .register(registry);

        // Timer for report generation duration
        this.reportGenerationTimer = Timer.builder("report_generation_duration_seconds")
                                          .description("Time taken to generate reports")
                                          .publishPercentileHistogram()
                                          .register(registry);

        // Gauge for job availability (1 = up, 0 = down)
        this.inventoryAvailability = new AtomicInteger(1);
        Gauge.builder("inventory_sync_availability", inventoryAvailability, AtomicInteger::get)
             .description("Inventory sync availability: 1 = up, 0 = down")
             .register(registry);
    }

    /**
     * Simulate user registration for throughput metric.
     */
    public void registerUser() {
        logger.info("Simulating user registration job...");
        // Simulated business logic (no delay here)
        userRegistrationThroughput.increment();
        logger.info("User registration metric incremented.");
    }

    /**
     * Simulate time-consuming report generation with manual timer.
     */
    public void generateReport() {
        logger.info("Simulating report generation...");

        long start = System.nanoTime();
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(200, 1001));
            logger.info("Report generation completed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Report generation was interrupted.");
        } finally {
            long end = System.nanoTime();
            reportGenerationTimer.record(end - start, TimeUnit.NANOSECONDS);
            logger.info("Report generation duration recorded.");
        }
    }

    /**
     * Simulate a payment job that may fail and increment failures manually.
     */
    public void processPayment() {
        logger.info("Simulating payment processing job...");
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            paymentFailures.increment();
            logger.error("Payment job failed. Failure counter incremented.");
            throw new RuntimeException("Simulated payment failure");
        }
        logger.info("Payment job succeeded.");
    }

    /**
     * Simulate inventory sync status that randomly flips between available/unavailable.
     */
    public String syncInventory() {
        boolean available = ThreadLocalRandom.current().nextBoolean();
        inventoryAvailability.set(available ? 1 : 0);
        String state = available ? "AVAILABLE" : "UNAVAILABLE";
        logger.info("Inventory sync is {}", state);
        return state;
    }
}
