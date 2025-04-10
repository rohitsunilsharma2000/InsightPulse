package com.example.insightpulse.filter;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Timer;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MetricsFilter extends OncePerRequestFilter {
    private final MeterRegistry meterRegistry;

    public MetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Capture start time
        long start = System.nanoTime();
        Exception exception = null;  // To track if an exception occurred

        try {
            // Proceed with the request
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // If an exception is caught, record it
            exception = e;
            throw e;  // Re-throw the exception after tracking it
        } finally {
            // Capture end time
            long end = System.nanoTime();

            // Get the status code after request completion
            int status = response.getStatus();

            // Determine the outcome based on the status code
            String outcome = (status >= 200 && status < 300) ? "SUCCESS" : "FAILURE";

            // If status code is 500, explicitly set outcome to FAILURE
            if (status == 500) {
                outcome = "FAILURE";
            }

            // Determine if there was an exception
            String exceptionTag = (exception != null) ? exception.getClass().getSimpleName() : "None";

            // Record the HTTP request duration with dynamic status, outcome, and exception
            Timer.builder("http_server_requests_seconds_count")
                 .tags("method", request.getMethod(),
                       "uri", request.getRequestURI(),
                       "exception", exceptionTag,
                       "outcome", outcome,
                       "status", String.valueOf(status))  // Dynamically set status code
                 .publishPercentileHistogram()
                 .register(meterRegistry)
                 .record(end - start, TimeUnit.NANOSECONDS);
        }
    }
}


