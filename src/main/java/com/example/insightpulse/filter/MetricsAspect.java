package com.example.insightpulse.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class MetricsAspect  {

    private final MeterRegistry meterRegistry;

    // Gauge to track overall job availability (you can update the logic dynamically)
//    private final Gauge jobAvailabilityGauge;

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

//        // Simple static gauge; replace with your own health indicator logic if needed
//        this.jobAvailabilityGauge = Gauge.builder("job_availability", () -> 1)
//                                         .description("Job availability (1 = available, 0 = unavailable)")
//                                         .register(meterRegistry);
    }



    /**
     * Pointcut around all methods inside service package
     */
    @Around("execution(* com.example.insightpulse.service..*(..))")
    public Object recordMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        Exception exception = null;

        // Get class and method name
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // Start a timer sample
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();

            // Throughput counter for method
            Counter.builder("job_throughput_total")
                   .description("Total job executions")
                   .tags("class", className, "method", methodName)
                   .register(meterRegistry)
                   .increment();

            // Success counter
            Counter.builder("service_success_total")
                   .description("Successful job executions")
                   .tags("class", className, "method", methodName)
                   .register(meterRegistry)
                   .increment();

            // Record success duration
            recordDuration(startTime, className, methodName, "SUCCESS", "None");

            return result;

        } catch (Exception e) {
            exception = e;

            // Failure counter with exception type
            Counter.builder("service_failures_total")
                   .description("Failed job executions")
                   .tags("class", className, "method", methodName, "exception", e.getClass().getSimpleName())
                   .register(meterRegistry)
                   .increment();

            // Record failure duration
            recordDuration(startTime, className, methodName, "FAILURE", e.getClass().getSimpleName());

            throw e;

        } finally {
            // Record the overall duration (without outcome/exception tag)
            sample.stop(Timer.builder("service_execution_duration_seconds")
                             .description("Execution duration for service method")
                             .tags("class", className, "method", methodName)
                             .publishPercentileHistogram()
                             .register(meterRegistry));
        }
    }

    /**
     * Records the duration for a method with detailed outcome/exception tagging.
     */
    private void recordDuration(long startTime, String className, String methodName, String outcome, String exception) {
        long duration = System.nanoTime() - startTime;

        Timer.builder("service_execution_duration_seconds")
             .description("Execution duration by outcome")
             .tags("class", className, "method", methodName, "outcome", outcome, "exception", exception)
             .publishPercentileHistogram()
             .register(meterRegistry)
             .record(duration, TimeUnit.NANOSECONDS);
    }

    // Stores dynamic availability per endpoint



}
