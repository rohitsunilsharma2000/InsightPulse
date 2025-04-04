//package com.example.prometheus_grafana_dashboard.cron;
//
//
//import io.micrometer.core.annotation.Counted;
//import io.micrometer.core.annotation.Timed;
//import io.micrometer.core.instrument.Gauge;
//import io.micrometer.core.instrument.MeterRegistry;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.Random;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//public class AnnotationBasedJobSchedulerVersion {
//
//    private static final Logger logger = LoggerFactory.getLogger(AnnotationBasedJobSchedulerVersion.class);
//    private final Random random = new Random();
//    private final AtomicInteger jobAvailability = new AtomicInteger(1); // default available
//
//    public AnnotationBasedJobSchedulerVersion(MeterRegistry meterRegistry) {
//        Gauge.builder("job_availability", jobAvailability, AtomicInteger::get)
//             .description("1 if job is available, 0 if unavailable")
//             .register(meterRegistry);
//    }
//
//    /**
//     * Job that runs every 1 second.
//     * Collects metrics on:
//     * - Throughput (Counted)
//     * - Failures (Counted)
//     * - Execution Duration (Timed)
//     */
//    @Scheduled(cron = "*/1 * * * * *")
//    @Timed(value = "job_execution_duration", description = "Time taken to execute the job")
//    @Counted(value = "job_throughput", description = "Total number of job executions")
//    public void processJob() {
//        logger.info("Running scheduled job...");
//        try {
//            // Simulate job work
//            Thread.sleep(random.nextInt(500));
//
//            // Simulate failure condition
//            if (random.nextDouble() < 0.2) {
//                jobAvailability.set(0);
//                logger.error("Job failed");
//                // Record failure
//                throw new RuntimeException("Simulated failure");
//            }
//
//            logger.info("Job executed successfully");
//
//        } catch (Exception e) {
//            // Record failure metric manually (as Micrometer doesn't support conditional @Counted)
//            logger.debug("Recording job_failures_total metric manually");
//            jobAvailability.set(0);
//            // Ideally handled via separate Counter
//        } finally {
//            jobAvailability.set(1);
//        }
//    }
//}
