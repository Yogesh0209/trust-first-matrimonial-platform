package com.tfmsp.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.ReportResolveRequest;
import com.tfmsp.backend.dto.ReportResponse;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.ModerationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/host/reports")
public class HostModerationController {

    private final ModerationService moderationService;
    private final UserRepository userRepository;

    public HostModerationController(ModerationService moderationService, UserRepository userRepository) {
        this.moderationService = moderationService;
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

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(moderationService.getHostReports(host.getBureauId()));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportResolveRequest req,
            Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(moderationService.resolveReport(id, req.getStatus(), host.getId()));
    }
}
