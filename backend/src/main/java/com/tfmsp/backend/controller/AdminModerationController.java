package com.tfmsp.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.ReportResponse;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.ModerationService;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminModerationController {

    private final ModerationService moderationService;
    private final UserRepository userRepository;

    public AdminModerationController(ModerationService moderationService, UserRepository userRepository) {
        this.moderationService = moderationService;
        this.userRepository = userRepository;
    }

    private User verifySuperAdmin(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Super Admin role required");
        }
        return user;
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports(Authentication auth) {
        verifySuperAdmin(auth);
        return ResponseEntity.ok(moderationService.getAllReports());
    }
}
