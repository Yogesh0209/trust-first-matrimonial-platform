package com.tfmsp.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfmsp.backend.service.SubscriptionExpiryService;

@RestController
@RequestMapping("/api/admin/subscriptions")
public class AdminSubscriptionJobController {

    private final SubscriptionExpiryService expiryService;

    public AdminSubscriptionJobController(SubscriptionExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    @PostMapping("/run-expiry-check")
    public ResponseEntity<Map<String, Integer>> run() {
        return ResponseEntity.ok(expiryService.runDailyCheck());
    }
}