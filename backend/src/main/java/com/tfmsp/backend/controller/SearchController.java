package com.tfmsp.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.SearchFilterCriteria;
import com.tfmsp.backend.dto.SearchResultResponse;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.PlanLimitService;
import com.tfmsp.backend.service.SearchService;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;
    private final UserRepository userRepository;
    private final PlanLimitService planLimitService;

    public SearchController(SearchService searchService, UserRepository userRepository,
            PlanLimitService planLimitService) {
        this.searchService = searchService;
        this.userRepository = userRepository;
        this.planLimitService = planLimitService;
    }

    private Long getAuthenticatedUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<List<SearchResultResponse>> search(
            @ModelAttribute SearchFilterCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return paged(searchService.searchHomeBureau(callerUserId, criteria), page, size);
    }

    @GetMapping("/shared")
    public ResponseEntity<List<SearchResultResponse>> searchShared(
            @ModelAttribute SearchFilterCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        userRepository.findById(callerUserId)
                .ifPresent(u -> planLimitService.requireCrossBureau(u.getBureauId()));
        return paged(searchService.searchShared(callerUserId, criteria), page, size);
    }

    private ResponseEntity<List<SearchResultResponse>> paged(List<SearchResultResponse> all, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50); // default 10, max 50
        int safePage = Math.max(page, 0);
        int from = Math.min(safePage * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(all.size()))
                .body(all.subList(from, to));
    }
}