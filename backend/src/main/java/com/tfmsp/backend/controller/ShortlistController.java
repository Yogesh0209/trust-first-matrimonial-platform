package com.tfmsp.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.SearchResultResponse;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.SearchService;

@RestController
@RequestMapping("/api/shortlists")
public class ShortlistController {

    private final SearchService searchService;
    private final UserRepository userRepository;

    public ShortlistController(SearchService searchService, UserRepository userRepository) {
        this.searchService = searchService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    @PostMapping("/{profileId}")
    public ResponseEntity<Map<String, String>> addToShortlist(
            @PathVariable Long profileId,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        searchService.addToShortlist(callerUserId, profileId);
        return ResponseEntity.ok(Map.of("message", "Profile added to shortlist successfully"));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Map<String, String>> removeFromShortlist(
            @PathVariable Long profileId,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        searchService.removeFromShortlist(callerUserId, profileId);
        return ResponseEntity.ok(Map.of("message", "Profile removed from shortlist successfully"));
    }

    @GetMapping
    public ResponseEntity<List<SearchResultResponse>> getShortlistedProfiles(Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(searchService.getShortlistedProfiles(callerUserId));
    }
}
