package com.example.insightpulse.controller;

import com.example.insightpulse.service.DummyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    private final DummyService dummyService;

    public DummyController(DummyService dummyService) {
        this.dummyService = dummyService;
    }

    // Endpoint to trigger job execution
    @GetMapping("/execute-dummy-job")
    public String executeJob() {
        try {
            dummyService.job_history_success();
            return "Job executed successfully";
        } catch (RuntimeException e) {
            return "Job execution failed: " + e.getMessage();
        }
    }
}
