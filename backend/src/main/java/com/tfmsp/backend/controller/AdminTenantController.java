package com.tfmsp.backend.controller;

import java.util.List;

import com.tfmsp.backend.dto.SubscriptionAssignRequest;
import com.tfmsp.backend.dto.SubscriptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.BureauResponse;
import com.tfmsp.backend.dto.BureauStatusUpdateRequest;
import com.tfmsp.backend.dto.PlanRequest;
import com.tfmsp.backend.entity.Plan;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.TenantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminTenantController {

    private final TenantService tenantService;
    private final UserRepository userRepository;

    public AdminTenantController(TenantService tenantService, UserRepository userRepository) {
        this.tenantService = tenantService;
        this.userRepository = userRepository;
    }

    private User verifySuperAdmin(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Super Admin role required");
        }
        return user;
    }

    @GetMapping("/tenants")
    public ResponseEntity<List<BureauResponse>> getBureaus(
            @RequestParam(required = false) String status,
            Authentication auth) {
        verifySuperAdmin(auth);
        return ResponseEntity.ok(tenantService.getAllBureaus(status));
    }

    @PutMapping("/tenants/{id}/status")
    public ResponseEntity<BureauResponse> updateBureauStatus(
            @PathVariable Long id,
            @Valid @RequestBody BureauStatusUpdateRequest req,
            Authentication auth) {
        User admin = verifySuperAdmin(auth);
        return ResponseEntity.ok(tenantService.updateBureauStatus(id, req.getStatus(), req.getReason(), admin.getId()));
    }

    @GetMapping("/plans")
    public ResponseEntity<List<Plan>> getAllPlans(Authentication auth) {
        verifySuperAdmin(auth);
        return ResponseEntity.ok(tenantService.getAllPlans());
    }
    
    @PostMapping("/tenants/{id}/subscription")
    public ResponseEntity<SubscriptionResponse> assignSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionAssignRequest req,
            Authentication auth) {
        User admin = verifySuperAdmin(auth);
        int months = req.getMonths() == null ? 1 : req.getMonths();
        return ResponseEntity.ok(
                tenantService.assignSubscription(id, req.getPlanId(), months, admin.getId()));
    }
    
    @PostMapping("/plans")
    public ResponseEntity<Plan> createPlan(@Valid @RequestBody PlanRequest req, Authentication auth) {
        User admin = verifySuperAdmin(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createPlan(req, admin.getId()));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<Plan> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequest req,
            Authentication auth) {
        User admin = verifySuperAdmin(auth);
        return ResponseEntity.ok(tenantService.updatePlan(id, req, admin.getId()));
    }
}
