package com.tfmsp.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.InterestRequest;
import com.tfmsp.backend.dto.InterestRespondRequest;
import com.tfmsp.backend.dto.InterestResponse;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.InteractionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

    private final InteractionService interactionService;
    private final UserRepository userRepository;

    public InterestController(InteractionService interactionService, UserRepository userRepository) {
        this.interactionService = interactionService;
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
    public ResponseEntity<InterestResponse> sendInterest(
            @Valid @RequestBody InterestRequest req,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        InterestResponse response = interactionService.sendInterest(callerUserId, req.getReceiverProfileId(), req.getNote());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<InterestResponse>> getSentInterests(Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(interactionService.getSentInterests(callerUserId));
    }

    @GetMapping("/received")
    public ResponseEntity<List<InterestResponse>> getReceivedInterests(Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(interactionService.getReceivedInterests(callerUserId));
    }

    @PutMapping("/{id}/respond")
    public ResponseEntity<InterestResponse> respondToInterest(
            @PathVariable Long id,
            @Valid @RequestBody InterestRespondRequest req,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(interactionService.respondToInterest(callerUserId, id, req.getAction()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> withdrawInterest(
            @PathVariable Long id,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        interactionService.withdrawInterest(callerUserId, id);
        return ResponseEntity.ok(Map.of("message", "Interest withdrawn successfully"));
    }
}
