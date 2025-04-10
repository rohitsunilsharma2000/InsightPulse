package com.example.insightpulse.filter;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiAvailabilityFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;
    private final Map<String, Integer> availabilityMap = new ConcurrentHashMap<>();

    public ApiAvailabilityFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String pattern = request.getRequestURI(); // fallback
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Try to get real route pattern (e.g., /api/users/{id})
            Object bestPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (bestPattern instanceof String) {
                pattern = (String) bestPattern;
            }

            String method = request.getMethod();
            String status = String.valueOf(response.getStatus());
            int availability = (response.getStatus() >= 500) ? 0 : 1;

            // Composite key
            String key = method + " " + pattern + " " + status;

            // Tags to be added to gauge
            final String finalMethod = method;
            final String finalPattern = pattern;
            final String finalStatus = status;

            // Insert or update availability
            availabilityMap.put(key, availability);

            // Only register gauge if it hasn't been already
            if (!gaugeExists("api_availability", finalMethod, finalPattern, finalStatus)) {
                Gauge.builder("api_availability", availabilityMap, map -> {
                         Integer value = map.get(key);
                         return value != null ? value : 1;
                     })
                     .description("API availability by method and URI pattern")
                     .tags("method", finalMethod, "uri", finalPattern, "status", finalStatus)
                     .register(meterRegistry);
            }
        }
    }

    private boolean gaugeExists(String metricName, String method, String uri, String status) {
        return meterRegistry.getMeters().stream().anyMatch(meter ->
                                                                   meter.getId().getName().equals(metricName)
                                                                           && method.equals(meter.getId().getTag("method"))
                                                                           && uri.equals(meter.getId().getTag("uri"))
                                                                           && status.equals(meter.getId().getTag("status"))
        );
    }
}
