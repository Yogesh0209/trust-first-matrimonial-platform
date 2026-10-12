package com.tfmsp.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.GovIdSubmissionRequest;
import com.tfmsp.backend.dto.SelfieSubmissionRequest;
import com.tfmsp.backend.dto.VerificationResponse;
import com.tfmsp.backend.dto.VerificationReviewRequest;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.entity.Verification;
import com.tfmsp.backend.enums.ReviewStatus;
import com.tfmsp.backend.enums.VerificationType;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.repository.VerificationRepository;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public VerificationService(VerificationRepository verificationRepository,
                               ProfileRepository profileRepository,
                               UserRepository userRepository) {
        this.verificationRepository = verificationRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public VerificationResponse submitSelfie(Long userId, SelfieSubmissionRequest request) {
        Profile profile = getProfileByUserId(userId);

        // FRD VER-04: At most three resubmissions per rolling 30 days
        checkResubmissionLimit(profile.getId(), VerificationType.SELFIE);

        Verification verification = new Verification();
        verification.setProfileId(profile.getId());
        verification.setType(VerificationType.SELFIE);
        verification.setFileUrl(request.getFileUrl().trim());
        verification.setStatus(ReviewStatus.PENDING);

        Verification saved = verificationRepository.save(verification);
        return mapToResponse(saved);
    }

    @Transactional
    public VerificationResponse submitGovId(Long userId, GovIdSubmissionRequest request) {
        Profile profile = getProfileByUserId(userId);

        // FRD VER-04: At most three resubmissions per rolling 30 days
        checkResubmissionLimit(profile.getId(), VerificationType.GOV_ID);

        Verification verification = new Verification();
        verification.setProfileId(profile.getId());
        verification.setType(VerificationType.GOV_ID);
        verification.setFileUrl(request.getFileUrl().trim());
        if (request.getFileUrlBack() != null && !request.getFileUrlBack().isBlank()) {
            verification.setFileUrlBack(request.getFileUrlBack().trim());
        }
        verification.setStatus(ReviewStatus.PENDING);

        Verification saved = verificationRepository.save(verification);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VerificationResponse> getReviewQueue(Long reviewerUserId) {
        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found"));

        // Oldest-first review queue per FRD VER-04
        List<Verification> queue = verificationRepository.findByStatusOrderBySubmittedAtAsc(ReviewStatus.PENDING);
        return queue.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public VerificationResponse reviewVerification(Long reviewerUserId, Long verificationId, VerificationReviewRequest request) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification case not found"));

        if (verification.getStatus() != ReviewStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This verification case has already been decided");
        }

        Profile candidateProfile = profileRepository.findById(verification.getProfileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate profile not found"));

        // FRD VER-10: A reviewer cannot approve their own identity evidence
        if (candidateProfile.getUserId().equals(reviewerUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reviewers cannot decide their own identity evidence");
        }

        verification.setStatus(request.getDecision());
        verification.setReviewedBy(reviewerUserId);
        verification.setReviewedAt(LocalDateTime.now());

        if (request.getDecision() == ReviewStatus.REJECTED) {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reason is required when rejecting verification");
            }
            verification.setRejectReason(request.getReason().trim());
        } else if (request.getDecision() == ReviewStatus.APPROVED) {
            // FRD VER-01 & Section 1.4: Upgrade Trust Level
            if (verification.getType() == VerificationType.SELFIE) {
                candidateProfile.setTrustLevel(Math.max(candidateProfile.getTrustLevel(), 2));
            } else if (verification.getType() == VerificationType.GOV_ID) {
                candidateProfile.setTrustLevel(3);
            }
            profileRepository.save(candidateProfile);
        }

        Verification saved = verificationRepository.save(verification);
        return mapToResponse(saved);
    }

    private void checkResubmissionLimit(Long profileId, VerificationType type) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentSubmissions = verificationRepository.countByProfileIdAndTypeAndSubmittedAtAfter(profileId, type, thirtyDaysAgo);
        if (recentSubmissions >= 3) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum 3 verification submissions allowed per 30-day window");
        }
    }

    private Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Please create your profile before submitting verification"));
    }

    private VerificationResponse mapToResponse(Verification v) {
        return new VerificationResponse(
                v.getId(),
                v.getProfileId(),
                v.getType(),
                v.getFileUrl(),
                v.getFileUrlBack(),
                v.getStatus(),
                v.getRejectReason(),
                v.getSubmittedAt(),
                v.getReviewedAt()
        );
    }
}
