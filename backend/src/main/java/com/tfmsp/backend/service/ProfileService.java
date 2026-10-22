package com.tfmsp.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.PhotoUploadRequest;
import com.tfmsp.backend.dto.PrivacyUpdateRequest;
import com.tfmsp.backend.dto.ProfilePhotoResponse;
import com.tfmsp.backend.dto.ProfileRequest;
import com.tfmsp.backend.dto.ProfileResponse;
import com.tfmsp.backend.dto.SharingUpdateRequest;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.entity.ProfilePhoto;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.ProfilePhotoRepository;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.UserRepository;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final UserRepository userRepository;
    private final ProfileAccessService profileAccessService;

    public ProfileService(ProfileRepository profileRepository,
            ProfilePhotoRepository profilePhotoRepository,
            UserRepository userRepository,
            ProfileAccessService profileAccessService) {
        this.profileRepository = profileRepository;
        this.profilePhotoRepository = profilePhotoRepository;
        this.userRepository = userRepository;
        this.profileAccessService = profileAccessService;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getOwnProfile(Long userId) {
        User user = getUser(userId);
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profile not found. Please complete your profile."));

        return mapToResponse(profile, user, true);
    }

    @Transactional
    public ProfileResponse createOrUpdateOwnProfile(Long userId, ProfileRequest request) {
        User user = getUser(userId);

        Profile profile = profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile newProfile = new Profile();
            newProfile.setUserId(userId);
            // Gender and date of birth come from registration
            newProfile.setGender(user.getGender() != null
                    ? user.getGender()
                    : com.tfmsp.backend.enums.Gender.OTHER);
            newProfile.setDateOfBirth(user.getDateOfBirth() != null
                    ? user.getDateOfBirth()
                    : LocalDate.now().minusYears(22));
            return newProfile;
        });

        profile.setMaritalStatus(request.getMaritalStatus());
        profile.setReligion(request.getReligion());
        profile.setMotherTongue(request.getMotherTongue());
        profile.setCity(request.getCity());
        profile.setHeightCm(request.getHeightCm());
        profile.setEducation(request.getEducation());
        profile.setOccupation(request.getOccupation());
        profile.setFamilyType(request.getFamilyType());
        profile.setAbout(request.getAbout());

        Profile saved = profileRepository.save(profile);

        // Activate account when email & phone are verified and required
        // profile fields are completed
        int completeness = calculateCompleteness(saved);
        if (user.getStatus() == UserStatus.PENDING && user.isEmailVerified() && user.isPhoneVerified()
                && completeness >= 80) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        return mapToResponse(saved, user, true);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(Long profileId, Long viewerUserId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        profileAccessService.assertCanView(viewerUserId, profile);

        boolean isOwner = profile.getUserId().equals(viewerUserId);

        // Hidden profiles cannot be viewed by others
        if (!isOwner && profile.getVisibility() == Visibility.HIDDEN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile is not available");
        }

        User user = getUser(profile.getUserId());
        return mapToResponse(profile, user, isOwner);
    }

    @Transactional
    public ProfilePhotoResponse addPhoto(Long userId, PhotoUploadRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Please create a profile first"));

        // Maximum 6 photos
        long photoCount = profilePhotoRepository.countByProfileId(profile.getId());
        if (photoCount >= 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 6 photos allowed");
        }

        ProfilePhoto photo = new ProfilePhoto();
        photo.setProfileId(profile.getId());
        photo.setPhotoUrl(request.getPhotoUrl().trim());

        // Exactly one primary photo rule
        if (photoCount == 0 || request.isPrimary()) {
            unsetPrimaryPhotos(profile.getId());
            photo.setPrimary(true);
        } else {
            photo.setPrimary(false);
        }

        ProfilePhoto saved = profilePhotoRepository.save(photo);
        return new ProfilePhotoResponse(saved.getId(), saved.getPhotoUrl(), saved.isPrimary(), saved.getUploadedAt());
    }

    @Transactional
    public void setPrimaryPhoto(Long userId, Long photoId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        ProfilePhoto target = profilePhotoRepository.findByIdAndProfileId(photoId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));

        unsetPrimaryPhotos(profile.getId());
        target.setPrimary(true);
        profilePhotoRepository.save(target);
    }

    @Transactional
    public void deletePhoto(Long userId, Long photoId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        ProfilePhoto photo = profilePhotoRepository.findByIdAndProfileId(photoId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));

        boolean wasPrimary = photo.isPrimary();
        profilePhotoRepository.delete(photo);

        // If primary photo was deleted, promote another photo as primary
        if (wasPrimary) {
            List<ProfilePhoto> remaining = profilePhotoRepository
                    .findByProfileIdOrderByIsPrimaryDescUploadedAtDesc(profile.getId());
            if (!remaining.isEmpty()) {
                ProfilePhoto newPrimary = remaining.get(0);
                newPrimary.setPrimary(true);
                profilePhotoRepository.save(newPrimary);
            }
        }
    }

    @Transactional
    public void updatePrivacy(Long userId, PrivacyUpdateRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        profile.setVisibility(request.getVisibility());
        profileRepository.save(profile);
    }

    private void unsetPrimaryPhotos(Long profileId) {
        profilePhotoRepository.findByProfileIdAndIsPrimaryTrue(profileId)
                .ifPresent(existing -> {
                    existing.setPrimary(false);
                    profilePhotoRepository.save(existing);
                });
    }
    
    @Transactional
    public void updateSharing(Long userId, SharingUpdateRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profile not found. Please complete your profile."));

        profile.setShareConsent(request.getConsent());
        profile.setShareConsentAt(LocalDateTime.now());
        profileRepository.save(profile);
    }
    

    /**
     * completed required fields / total required fields * 100
     */
    public int calculateCompleteness(Profile profile) {
        int totalRequired = 7;
        int completed = 0;

        if (profile.getGender() != null)
            completed++;
        if (profile.getDateOfBirth() != null)
            completed++;
        if (profile.getMaritalStatus() != null)
            completed++;
        if (profile.getMotherTongue() != null && !profile.getMotherTongue().isBlank())
            completed++;
        if (profile.getCity() != null && !profile.getCity().isBlank())
            completed++;
        if (profile.getEducation() != null && !profile.getEducation().isBlank())
            completed++;
        if (profile.getOccupation() != null && !profile.getOccupation().isBlank())
            completed++;

        return Math.round(((float) completed / totalRequired) * 100);
    }

    /**
     * Deterministic trust score (0 - 100)
     */
    public int calculateTrustScore(User user, Profile profile, int completeness) {
        int score = 0;
        if (user.isEmailVerified())
            score += 10;
        if (user.isPhoneVerified())
            score += 10;
        if (profile.getTrustLevel() >= 2)
            score += 25; // Approved selfie
        if (profile.getTrustLevel() >= 3)
            score += 30; // Approved ID
        if (completeness >= 80)
            score += 10;
        if (user.getLastActiveAt() != null && user.getLastActiveAt().isAfter(LocalDateTime.now().minusDays(30))) {
            score += 15;
        }

        return Math.min(100, Math.max(0, score));
    }

    /**
     * Band classification
     */
    public String getTrustBand(int score) {
        if (score >= 70)
            return "HIGH";
        if (score >= 40)
            return "MEDIUM";
        return "LOW";
    }

    private ProfileResponse mapToResponse(Profile profile, User user, boolean isOwner) {
        ProfileResponse res = new ProfileResponse();
        res.setId(profile.getId());
        res.setUserId(profile.getUserId());
        res.setFullName(user.getFullName());
        res.setGender(profile.getGender());
        res.setDateOfBirth(profile.getDateOfBirth());
        res.setAge(Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears());
        res.setMaritalStatus(profile.getMaritalStatus());
        res.setReligion(profile.getReligion());
        res.setMotherTongue(profile.getMotherTongue());
        res.setCity(profile.getCity());
        res.setHeightCm(profile.getHeightCm());
        res.setEducation(profile.getEducation());
        res.setOccupation(profile.getOccupation());
        res.setFamilyType(profile.getFamilyType());
        res.setAbout(profile.getAbout());
        res.setTrustLevel(profile.getTrustLevel());

        int completeness = calculateCompleteness(profile);
        int trustScore = calculateTrustScore(user, profile, completeness);

        res.setCompleteness(completeness);
        res.setTrustBand(getTrustBand(trustScore));

        // Exact numeric score is visible only to owner
        if (isOwner) {
            res.setTrustScore(trustScore);
        }

        res.setVisibility(profile.getVisibility());
        res.setUpdatedAt(profile.getUpdatedAt());

        List<ProfilePhoto> photos = profilePhotoRepository
                .findByProfileIdOrderByIsPrimaryDescUploadedAtDesc(profile.getId());
        res.setPhotos(photos.stream()
                .map(p -> new ProfilePhotoResponse(p.getId(), p.getPhotoUrl(), p.isPrimary(), p.getUploadedAt()))
                .collect(Collectors.toList()));

        photos.stream()
                .filter(ProfilePhoto::isPrimary)
                .findFirst()
                .ifPresent(primary -> res.setPrimaryPhotoUrl(primary.getPhotoUrl()));

        return res;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
    }
}
