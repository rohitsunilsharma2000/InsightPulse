Absolutely! Below is the **updated version** of your **Spring Boot code** using `@Timed` and `@Counted` annotations in the **service layer** (not in controller), which is the best practice for tracking:

- ✅ Job Throughput
- ✅ Job Execution Duration
- ✅ Job Failures
- ✅ Job Availability

With clear inline comments and logging.

---

## ✅ `JobController.java` (No metrics annotations here)

```java
package com.example.metricsdemo.controller;

import com.example.metricsdemo.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/register-user")
    public ResponseEntity<String> registerUser() {
        jobService.registerUser();
        return ResponseEntity.ok("User registered.");
    }

    @GetMapping("/generate-report")
    public ResponseEntity<String> generateReport() {
        jobService.generateReport();
        return ResponseEntity.ok("Report generated.");
    }

    @GetMapping("/process-payment")
    public ResponseEntity<String> processPayment() {
        try {
            jobService.processPayment();
            return ResponseEntity.ok("Payment processed.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payment failed: " + e.getMessage());
        }
    }

    @GetMapping("/inventory-sync")
    public ResponseEntity<String> inventorySync() {
        String status = jobService.syncInventory();
        return ResponseEntity.ok("Inventory Sync Status: " + status);
    }
}
```

---

## ✅ `JobService.java` (Metrics applied here)

```java
package com.example.metricsdemo.service;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    private final Counter paymentFailures;
    private final AtomicInteger inventoryAvailability;

    public JobService(MeterRegistry registry) {
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
    public String syncInventory() {
        boolean available = ThreadLocalRandom.current().nextBoolean();
        inventoryAvailability.set(available ? 1 : 0);
        String state = available ? "AVAILABLE" : "UNAVAILABLE";
        logger.info("Inventory sync is {}", state);
        return state;
    }
}
```

---

## ✅ Metrics You’ll See in Prometheus

| Metric Name                          | Description                               |
|--------------------------------------|-------------------------------------------|
| `user_registration_throughput_total` | Number of user registration jobs run      |
| `report_generation_duration_seconds` | Execution time of report generation jobs  |
| `payment_failures_total`             | Count of payment failures                 |
| `inventory_sync_availability`        | 1 = available, 0 = unavailable            |

---

## ✅ Example Grafana PromQL Queries

```promql
rate(user_registration_throughput_total[1m])
rate(payment_failures_total[1m])
rate(report_generation_duration_seconds_sum[1m]) / rate(report_generation_duration_seconds_count[1m])
inventory_sync_availability
```

---

Let me know if you'd like:
- 📦 A full Maven project with this structure
- 📊 A Grafana dashboard JSON to import
- 🔔 Prometheus alert rules (e.g., failure rate thresholds, availability drop)

Ready to turn this into a Dockerized monitoring demo too if needed!Here are the `curl` commands to test each of your **job metric APIs**, simulating real use cases for **Prometheus + Grafana monitoring**:

---

## ✅ 1. **Job Throughput** — User Registration

```bash
curl http://localhost:8080/jobs/register-user
```

📈 **Metric exposed:** `user_registration_throughput_total`

---

## ✅ 2. **Job Execution Duration** — Report Generation

```bash
curl http://localhost:8080/jobs/generate-report
```

📈 **Metric exposed:** `report_generation_duration_seconds`

---

## ✅ 3. **Job Failures** — Payment Processing (might fail)

```bash
curl http://localhost:8080/jobs/process-payment
```

📈 **Metric exposed (manually):** `payment_failures_total`

*Run this multiple times to trigger some failures (30% failure chance).*

---

## ✅ 4. **Job Availability** — Inventory Sync

```bash
curl http://localhost:8080/jobs/inventory-sync
```

📈 **Metric exposed:** `inventory_sync_availability`

*Randomly toggles between 1 (available) and 0 (unavailable).*

---

## 🔍 View All Metrics

Once the above endpoints are hit at least once, go to:

```bash
curl http://localhost:8080/actuator/prometheus
```

Look for these in the output:
- `user_registration_throughput_total`
- `report_generation_duration_seconds_count`
- `payment_failures_total`
- `inventory_sync_availability`

---

Let me know if you want to:
- Run these in a loop to generate sample data
- Load test these endpoints
- Auto-generate alerts if availability = 0 for 2 minutes, etc.
- Awesome! Let’s break each one down for you and give you **ready-to-run solutions**:

---

## ✅ 1. **Run These in a Loop to Generate Sample Data**

You can use a basic `bash` loop to repeatedly call your endpoints and simulate real traffic:

```bash
#!/bin/bash

for i in {1..100}; do
  curl -s http://localhost:8080/jobs/register-user
  curl -s http://localhost:8080/jobs/generate-report
  curl -s http://localhost:8080/jobs/process-payment
  curl -s http://localhost:8080/jobs/inventory-sync
  sleep 2  # pause between rounds
done
```

🔧 Save this as `simulate_jobs.sh` and run:

```bash
chmod +x simulate_jobs.sh
./simulate_jobs.sh
```

---

## 🚀 2. **Load Test These Endpoints (Using `hey` or `ab`)**

### ✅ Using [`hey`](https://github.com/rakyll/hey) (modern & better than `ab`)

Install:

```bash
brew install hey
```

Test high throughput for user registration:

```bash
hey -n 1000 -c 50 http://localhost:8080/jobs/register-user
```

- `-n 1000`: total number of requests
- `-c 50`: concurrent requests

### ✅ Using `ab` (Apache Benchmark - older)

```bash
ab -n 500 -c 20 http://localhost:8080/jobs/generate-report
```

---

## 🔔 3. **Auto-Generate Alerts in Prometheus (for Grafana too)**

### 📄 `prometheus.yml` Alerting Rules

Add this to your `prometheus.yml`:

```yaml
rule_files:
  - alert.rules.yml
```

Then create `alert.rules.yml`:

```yaml
groups:
  - name: job_alerts
    rules:
      - alert: JobDown
        expr: inventory_sync_availability == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Inventory sync is down"
          description: "Inventory sync job has been unavailable for 2 minutes."
```

📌 This triggers if the job stays **unavailable (0)** for **2 minutes**.

---

### 📨 Want to Send Alerts to Email/Slack?

Add **Alertmanager** and configure a `docker-compose` service with SMTP/Slack/etc. Let me know, and I’ll help set it up for your stack.

---

### ✅ Optional: Auto-Provision Grafana Panels?

I can generate a **Grafana dashboard JSON** that:
- Graphs each metric
- Shows average job durations, throughput, failure rates
- Includes visual alert indicators

---

Let me know which one you want help with next:
- ✅ Alertmanager setup with Slack/email
- ✅ Grafana dashboard JSON
- ✅ Docker Compose file including Prometheus rules
- ✅ Realistic job simulation with concurrency and logs