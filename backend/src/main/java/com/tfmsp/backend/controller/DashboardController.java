package com.tfmsp.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.AdminDashboardResponse;
import com.tfmsp.backend.dto.AuditLogResponse;
import com.tfmsp.backend.dto.HostDashboardResponse;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.DashboardService;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    private User verifyHost(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() != Role.HOST && user.getRole() != Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Host role required");
        }
        if (user.getBureauId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any bureau");
        }
        return user;
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

    @GetMapping("/host/dashboard")
    public ResponseEntity<HostDashboardResponse> getHostDashboard(Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(dashboardService.getHostDashboard(host.getBureauId()));
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard(Authentication auth) {
        verifySuperAdmin(auth);
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/admin/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "50") int limit,
            Authentication auth) {
        verifySuperAdmin(auth);
        return ResponseEntity.ok(dashboardService.getAuditLogs(limit));
    }
    
    @GetMapping("/host/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getHostAuditLogs(
            @RequestParam(defaultValue = "50") int limit,
            Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(dashboardService.getHostAuditLogs(host.getBureauId(), limit));
    }
}
