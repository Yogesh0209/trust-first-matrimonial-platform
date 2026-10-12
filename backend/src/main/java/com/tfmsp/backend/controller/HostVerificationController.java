package com.tfmsp.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tfmsp.backend.dto.VerificationResponse;
import com.tfmsp.backend.dto.VerificationReviewRequest;
import com.tfmsp.backend.service.VerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/host/verifications")
public class HostVerificationController {

    private final VerificationService verificationService;

    public HostVerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping
    public ResponseEntity<List<VerificationResponse>> getReviewQueue(Principal principal) {
        Long reviewerUserId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(verificationService.getReviewQueue(reviewerUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VerificationResponse> reviewVerification(
            @PathVariable Long id,
            @Valid @RequestBody VerificationReviewRequest request,
            Principal principal) {
        Long reviewerUserId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(verificationService.reviewVerification(reviewerUserId, id, request));
    }
}
