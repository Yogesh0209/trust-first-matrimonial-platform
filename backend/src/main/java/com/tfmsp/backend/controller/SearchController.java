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
import com.tfmsp.backend.service.SearchService;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;
    private final UserRepository userRepository;

    public SearchController(SearchService searchService, UserRepository userRepository) {
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

    @GetMapping
    public ResponseEntity<List<SearchResultResponse>> searchHomeBureau(
            @ModelAttribute SearchFilterCriteria criteria,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(searchService.searchHomeBureau(callerUserId, criteria));
    }

    @GetMapping("/shared")
    public ResponseEntity<List<SearchResultResponse>> searchShared(
            @ModelAttribute SearchFilterCriteria criteria,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(searchService.searchShared(callerUserId, criteria));
    }
}
