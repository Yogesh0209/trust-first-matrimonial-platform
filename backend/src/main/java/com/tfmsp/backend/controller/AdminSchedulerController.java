package com.tfmsp.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.AuditLogResponse;
import com.tfmsp.backend.service.FreshnessService;

@RestController
@RequestMapping("/api/admin/scheduler-runs")
public class AdminSchedulerController {

    private final FreshnessService freshnessService;

    public AdminSchedulerController(FreshnessService freshnessService) {
        this.freshnessService = freshnessService;
    }

    private void requireSuperAdmin(Authentication auth) {
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!isSuperAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Super Admin role required");
        }
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> recentRuns(
            @RequestParam(defaultValue = "20") int limit,
            Authentication auth) {
        requireSuperAdmin(auth);
        return ResponseEntity.ok(freshnessService.getRecentRuns(limit));
    }

    @PostMapping("/freshness")
    public ResponseEntity<Map<String, Object>> runFreshnessNow(Authentication auth) {
        requireSuperAdmin(auth);
        int hidden = freshnessService.sweepInactiveProfiles();
        return ResponseEntity.ok(Map.of(
                "message", "Freshness sweep completed",
                "profilesHidden", hidden,
                "ranAt", LocalDateTime.now().toString()));
    }
}