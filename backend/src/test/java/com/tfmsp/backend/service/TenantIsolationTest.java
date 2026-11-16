package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.VerificationResponse;
import com.tfmsp.backend.dto.VerificationReviewRequest;
import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.entity.Report;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.entity.Verification;
import com.tfmsp.backend.enums.ReportStatus;
import com.tfmsp.backend.enums.ReviewStatus;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.VerificationType;
import com.tfmsp.backend.repository.AuditLogRepository;
import com.tfmsp.backend.repository.ProfileRepository;
import com.tfmsp.backend.repository.ReportRepository;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.repository.VerificationRepository;

/** A host of bureau 1 must never see or decide anything that belongs to bureau 2. */
@ExtendWith(MockitoExtension.class)
class TenantIsolationTest {

    @Mock private VerificationRepository verificationRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private AuditLogRepository auditLogRepository;

    private VerificationService verificationService;
    private ModerationService moderationService;

    private User hostOfBureau1;
    private User candidateOfBureau2;
    private Profile candidateProfile;

    @BeforeEach
    void setUp() {
        verificationService = new VerificationService(verificationRepository, profileRepository, userRepository);
        moderationService = new ModerationService(reportRepository, profileRepository, userRepository,
                auditLogRepository);

        hostOfBureau1 = new User();
        hostOfBureau1.setId(100L);
        hostOfBureau1.setRole(Role.HOST);
        hostOfBureau1.setBureauId(1L);

        candidateOfBureau2 = new User();
        candidateOfBureau2.setId(5L);
        candidateOfBureau2.setRole(Role.END_USER);
        candidateOfBureau2.setBureauId(2L);

        candidateProfile = new Profile();
        candidateProfile.setId(50L);
        candidateProfile.setUserId(5L);
    }

    @Test
    void host_cannotDecideVerification_ofAnotherBureau() {
        Verification v = new Verification();
        v.setId(7L);
        v.setProfileId(50L);
        v.setType(VerificationType.SELFIE);
        v.setStatus(ReviewStatus.PENDING);

        when(verificationRepository.findById(7L)).thenReturn(Optional.of(v));
        when(userRepository.findById(100L)).thenReturn(Optional.of(hostOfBureau1));
        when(profileRepository.findById(50L)).thenReturn(Optional.of(candidateProfile));
        when(userRepository.findById(5L)).thenReturn(Optional.of(candidateOfBureau2));

        VerificationReviewRequest req = new VerificationReviewRequest();
        req.setDecision(ReviewStatus.APPROVED);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> verificationService.reviewVerification(100L, 7L, req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(verificationRepository, never()).save(any());
    }

    @Test
    void host_doesNotSee_verificationQueue_ofAnotherBureau() {
        Verification v = new Verification();
        v.setId(7L);
        v.setProfileId(50L);
        v.setType(VerificationType.SELFIE);
        v.setStatus(ReviewStatus.PENDING);

        when(userRepository.findById(100L)).thenReturn(Optional.of(hostOfBureau1));
        when(verificationRepository.findByStatusOrderBySubmittedAtAsc(ReviewStatus.PENDING))
                .thenReturn(List.of(v));
        when(profileRepository.findById(50L)).thenReturn(Optional.of(candidateProfile));
        when(userRepository.findById(5L)).thenReturn(Optional.of(candidateOfBureau2));

        List<VerificationResponse> queue = verificationService.getReviewQueue(100L);

        assertTrue(queue.isEmpty());
    }

    @Test
    void host_cannotResolveReport_aboutMemberOfAnotherBureau() {
        Report report = new Report();
        report.setId(9L);
        report.setReportedProfileId(50L);
        report.setStatus(ReportStatus.OPEN);

        when(reportRepository.findById(9L)).thenReturn(Optional.of(report));
        when(profileRepository.findById(50L)).thenReturn(Optional.of(candidateProfile));
        when(userRepository.findById(5L)).thenReturn(Optional.of(candidateOfBureau2));

        // bureauScope = 1 means "this host belongs to bureau 1"
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> moderationService.resolveReport(9L, ReportStatus.REMOVED, 100L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(reportRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}