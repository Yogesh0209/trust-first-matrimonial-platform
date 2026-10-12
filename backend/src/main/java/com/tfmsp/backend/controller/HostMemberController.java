package com.tfmsp.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.MemberStatusUpdateRequest;
import com.tfmsp.backend.dto.MemberSummaryResponse;
import com.tfmsp.backend.dto.SubscriptionResponse;
import com.tfmsp.backend.dto.SubscriptionUpgradeRequest;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.TenantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/host")
public class HostMemberController {

    private final TenantService tenantService;
    private final UserRepository userRepository;

    public HostMemberController(TenantService tenantService, UserRepository userRepository) {
        this.tenantService = tenantService;
        this.userRepository = userRepository;
    }

    private User verifyHost(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() != Role.HOST && user.getRole() != Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Host role required");
        }
        if (user.getBureauId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any bureau");
        }
        return user;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberSummaryResponse>> getMembers(Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(tenantService.getHostMembers(host.getBureauId()));
    }

    @PutMapping("/members/{id}/status")
    public ResponseEntity<MemberSummaryResponse> updateMemberStatus(
            @PathVariable Long id,
            @Valid @RequestBody MemberStatusUpdateRequest req,
            Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(tenantService.updateMemberStatus(host.getBureauId(), id, req.getStatus(), host.getId()));
    }

    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> getSubscription(Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(tenantService.getBureauSubscription(host.getBureauId()));
    }

    @PostMapping("/subscription/upgrade")
    public ResponseEntity<SubscriptionResponse> upgradeSubscription(
            @Valid @RequestBody SubscriptionUpgradeRequest req,
            Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(tenantService.upgradeSubscription(host.getBureauId(), req.getPlanId(), host.getId()));
    }
}
