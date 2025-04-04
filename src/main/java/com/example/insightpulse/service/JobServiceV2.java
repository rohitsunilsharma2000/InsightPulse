package com.example.insightpulse.service;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JobServiceV2 {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceV2.class);
    private final Counter paymentFailures;
    private final AtomicInteger inventoryAvailability;

    public JobServiceV2 ( MeterRegistry registry) {
        // Manual counter for failed jobs
        this.paymentFailures = Counter.builder("payment_failures_total")
                                      .description("Number of failed payment attempts")
                                      .register(registry);

        // Job availability gauge (1 = up, 0 = down)
        this.inventoryAvailability = new AtomicInteger(1);
        Gauge.builder("inventory_sync_availability", inventoryAvailability, AtomicInteger::get)
             .description("Inventory sync availability: 1 = up, 0 = down")
             .register(registry);
    }

    /**
     * Simulate user registration for throughput.
     */
    @Counted(value = "user_registration_throughput", description = "Total number of user registrations")
    public void registerUser() {
        logger.info("Simulating user registration job.");
        // Simulated logic
    }

    /**
     * Simulate time-consuming report generation.
     */
    @Timed(value = "report_generation_duration", description = "Time taken to generate reports")
    public void generateReport() {
        logger.info("Simulating report generation...");
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(200, 1001)); // realistic time delay
            logger.info("Report generation completed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Report generation was interrupted.");
        }
    }

    /**
     * Simulate a job with occasional failures.
     */
    public void processPayment() {
        logger.info("Simulating payment processing job...");
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            logger.error("Payment job failed. Recording failure.");
            paymentFailures.increment(); // manual metric
            throw new RuntimeException("Simulated payment failure");
        }
        logger.info("Payment job succeeded.");
    }

    /**
     * Simulate inventory sync with toggleable availability.
     */
    public String   syncInventory() {
        boolean available = ThreadLocalRandom.current().nextBoolean();
        inventoryAvailability.set(available ? 1 : 0);
        String state = available ? "AVAILABLE" : "UNAVAILABLE";
        logger.info("Inventory sync is {}", state);
        return state;
    }
}
