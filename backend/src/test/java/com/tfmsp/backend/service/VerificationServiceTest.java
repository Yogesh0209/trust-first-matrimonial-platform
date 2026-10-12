package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        verificationService = new VerificationService(verificationRepository, profileRepository, userRepository);
    }

    @Test
    void submitSelfie_Success() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setUserId(1L);

        SelfieSubmissionRequest req = new SelfieSubmissionRequest();
        req.setFileUrl("https://example.com/selfie.jpg");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(verificationRepository.countByProfileIdAndTypeAndSubmittedAtAfter(eq(10L), eq(VerificationType.SELFIE), any()))
                .thenReturn(0L);
        when(verificationRepository.save(any(Verification.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationResponse res = verificationService.submitSelfie(1L, req);

        assertNotNull(res);
        assertEquals(ReviewStatus.PENDING, res.getStatus());
        assertEquals(VerificationType.SELFIE, res.getType());
    }

    @Test
    void submitSelfie_Exceeds3Submissions_ThrowsTooManyRequests() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setUserId(1L);

        SelfieSubmissionRequest req = new SelfieSubmissionRequest();
        req.setFileUrl("https://example.com/selfie.jpg");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(verificationRepository.countByProfileIdAndTypeAndSubmittedAtAfter(eq(10L), eq(VerificationType.SELFIE), any()))
                .thenReturn(3L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> verificationService.submitSelfie(1L, req));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Maximum 3 verification submissions"));
    }

    @Test
    void submitGovId_Success() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setUserId(1L);

        GovIdSubmissionRequest req = new GovIdSubmissionRequest();
        req.setFileUrl("https://example.com/id-front.jpg");
        req.setDocumentType("AADHAAR");
        req.setMaskedReference("1234");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(verificationRepository.countByProfileIdAndTypeAndSubmittedAtAfter(eq(10L), eq(VerificationType.GOV_ID), any()))
                .thenReturn(0L);
        when(verificationRepository.save(any(Verification.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationResponse res = verificationService.submitGovId(1L, req);

        assertNotNull(res);
        assertEquals(VerificationType.GOV_ID, res.getType());
        assertEquals(ReviewStatus.PENDING, res.getStatus());
    }

    @Test
    void reviewVerification_ApproveSelfie_UpgradesToLevel2() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setUserId(5L);
        profile.setTrustLevel(1);

        Verification v = new Verification();
        v.setId(100L);
        v.setProfileId(10L);
        v.setType(VerificationType.SELFIE);
        v.setStatus(ReviewStatus.PENDING);

        VerificationReviewRequest req = new VerificationReviewRequest();
        req.setDecision(ReviewStatus.APPROVED);

        when(verificationRepository.findById(100L)).thenReturn(Optional.of(v));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(verificationRepository.save(any(Verification.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationResponse res = verificationService.reviewVerification(1L, 100L, req); // Reviewer is user 1

        assertEquals(ReviewStatus.APPROVED, res.getStatus());
        assertEquals(2, profile.getTrustLevel(), "Approving selfie must upgrade to Level 2 per FRD Section 1.4");
        verify(profileRepository).save(profile);
    }

    @Test
    void reviewVerification_ApproveGovId_UpgradesToLevel3() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setUserId(5L);
        profile.setTrustLevel(2);

        Verification v = new Verification();
        v.setId(100L);
        v.setProfileId(10L);
        v.setType(VerificationType.GOV_ID);
        v.setStatus(ReviewStatus.PENDING);

        VerificationReviewRequest req = new VerificationReviewRequest();
        req.setDecision(ReviewStatus.APPROVED);

        when(verificationRepository.findById(100L)).thenReturn(Optional.of(v));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(verificationRepository.save(any(Verification.class))).thenAnswer(inv -> inv.getArgument(0));

        VerificationResponse res = verificationService.reviewVerification(1L, 100L, req);

        assertEquals(ReviewStatus.APPROVED, res.getStatus());
        assertEquals(3, profile.getTrustLevel(), "Approving Gov ID must upgrade to Level 3 per FRD Section 1.4");
        verify(profileRepository).save(profile);
    }

    @Test
    void reviewVerification_Reject_RequiresReason() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setUserId(5L);

        Verification v = new Verification();
        v.setId(100L);
        v.setProfileId(10L);
        v.setStatus(ReviewStatus.PENDING);

        VerificationReviewRequest req = new VerificationReviewRequest();
        req.setDecision(ReviewStatus.REJECTED);
        req.setReason(""); // Missing reason

        when(verificationRepository.findById(100L)).thenReturn(Optional.of(v));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> verificationService.reviewVerification(1L, 100L, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("reason is required"));
    }

    @Test
    void reviewVerification_SelfReview_ThrowsForbidden() {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setUserId(1L); // Candidate is user 1

        Verification v = new Verification();
        v.setId(100L);
        v.setProfileId(10L);
        v.setStatus(ReviewStatus.PENDING);

        VerificationReviewRequest req = new VerificationReviewRequest();
        req.setDecision(ReviewStatus.APPROVED);

        when(verificationRepository.findById(100L)).thenReturn(Optional.of(v));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));

        // Reviewer is ALSO user 1
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> verificationService.reviewVerification(1L, 100L, req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("cannot decide their own"));
    }
}
