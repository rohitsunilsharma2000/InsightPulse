package com.example.insightpulse.controller;

import com.example.insightpulse.service.JobService;
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

