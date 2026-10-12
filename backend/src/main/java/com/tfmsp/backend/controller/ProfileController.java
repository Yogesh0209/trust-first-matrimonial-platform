package com.tfmsp.backend.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tfmsp.backend.dto.PhotoUploadRequest;
import com.tfmsp.backend.dto.PrivacyUpdateRequest;
import com.tfmsp.backend.dto.ProfilePhotoResponse;
import com.tfmsp.backend.dto.ProfileRequest;
import com.tfmsp.backend.dto.ProfileResponse;
import com.tfmsp.backend.service.ProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(profileService.getOwnProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid @RequestBody ProfileRequest request,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(profileService.createOrUpdateOwnProfile(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfileById(
            @PathVariable Long id,
            Principal principal) {
        Long viewerUserId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(profileService.getPublicProfile(id, viewerUserId));
    }

    @PostMapping("/me/photos")
    public ResponseEntity<ProfilePhotoResponse> addPhoto(
            @Valid @RequestBody PhotoUploadRequest request,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addPhoto(userId, request));
    }

    @PutMapping("/me/photos/{photoId}/primary")
    public ResponseEntity<Map<String, String>> setPrimaryPhoto(
            @PathVariable Long photoId,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        profileService.setPrimaryPhoto(userId, photoId);
        return ResponseEntity.ok(Map.of("message", "Primary photo updated successfully"));
    }

    @DeleteMapping("/me/photos/{photoId}")
    public ResponseEntity<Map<String, String>> deletePhoto(
            @PathVariable Long photoId,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        profileService.deletePhoto(userId, photoId);
        return ResponseEntity.ok(Map.of("message", "Photo deleted successfully"));
    }

    @PutMapping("/me/privacy")
    public ResponseEntity<Map<String, String>> updatePrivacy(
            @Valid @RequestBody PrivacyUpdateRequest request,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        profileService.updatePrivacy(userId, request);
        return ResponseEntity.ok(Map.of("message", "Privacy settings updated successfully"));
    }
}
