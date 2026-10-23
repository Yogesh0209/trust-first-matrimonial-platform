package com.tfmsp.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.ReportResponse;
import com.tfmsp.backend.dto.ReportSubmitRequest;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.ModerationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ModerationService moderationService;
    private final UserRepository userRepository;

    public ReportController(ModerationService moderationService, UserRepository userRepository) {
        this.moderationService = moderationService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<ReportResponse> submitReport(
            @Valid @RequestBody ReportSubmitRequest req,
            Authentication auth) {
        Long reporterUserId = getAuthenticatedUserId(auth);
        ReportResponse response = moderationService.submitReport(reporterUserId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
