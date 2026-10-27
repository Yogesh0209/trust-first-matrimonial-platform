package com.tfmsp.backend.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tfmsp.backend.dto.GovIdSubmissionRequest;
import com.tfmsp.backend.dto.SelfieSubmissionRequest;
import com.tfmsp.backend.dto.VerificationResponse;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.PlanLimitService;
import com.tfmsp.backend.service.VerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private final VerificationService verificationService;
    private final UserRepository userRepository;
    private final PlanLimitService planLimitService;
    
    public VerificationController(VerificationService verificationService,
            UserRepository userRepository, PlanLimitService planLimitService) {
        this.verificationService = verificationService;
        this.userRepository = userRepository;
        this.planLimitService = planLimitService;
    }

    @PostMapping("/selfie")
    public ResponseEntity<VerificationResponse> submitSelfie(
            @Valid @RequestBody SelfieSubmissionRequest request,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.submitSelfie(userId, request));
    }

    @PostMapping("/id")
    public ResponseEntity<VerificationResponse> submitGovId(
            @Valid @RequestBody GovIdSubmissionRequest request,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        userRepository.findById(userId).ifPresent(u -> planLimitService.requireGovIdVerification(u.getBureauId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationService.submitGovId(userId, request));
    }
    
    @GetMapping("/me")
    public ResponseEntity<java.util.List<VerificationResponse>> myVerifications(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(verificationService.getMyVerifications(userId));
    }
}
