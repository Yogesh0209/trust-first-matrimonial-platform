package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.ReportResponse;
import com.tfmsp.backend.dto.ReportSubmitRequest;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.ReportReason;
import com.tfmsp.backend.enums.ReportStatus;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.*;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    private ModerationService moderationService;

    @BeforeEach
    void setUp() {
        moderationService = new ModerationService(
                reportRepository,
                profileRepository,
                userRepository,
                auditLogRepository
        );
    }

    @Test
    void submitReport_Success() {
        Profile reporterProfile = new Profile();
        reporterProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(reporterProfile));

        Profile reportedProfile = new Profile();
        reportedProfile.setId(20L);
        reportedProfile.setUserId(2L);
        when(profileRepository.findById(20L)).thenReturn(Optional.of(reportedProfile));

        when(reportRepository.existsByReporterProfileIdAndReportedProfileId(10L, 20L)).thenReturn(false);
        when(reportRepository.countByReportedProfileIdAndStatus(20L, ReportStatus.OPEN)).thenReturn(1L);

        Report saved = new Report(10L, 20L, ReportReason.FAKE_PHOTOS, "Photos are of an actor");
        saved.setId(100L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);

        ReportSubmitRequest req = new ReportSubmitRequest();
        req.setReportedProfileId(20L);
        req.setReason(ReportReason.FAKE_PHOTOS);
        req.setDetails("Photos are of an actor");

        ReportResponse resp = moderationService.submitReport(1L, req);

        assertNotNull(resp);
        assertEquals(ReportReason.FAKE_PHOTOS, resp.getReason());
        assertEquals(ReportStatus.OPEN, resp.getStatus());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void submitReport_SelfReport_ThrowsBadRequest() {
        Profile reporterProfile = new Profile();
        reporterProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(reporterProfile));

        ReportSubmitRequest req = new ReportSubmitRequest();
        req.setReportedProfileId(10L); // self!

        assertThrows(ResponseStatusException.class, () -> moderationService.submitReport(1L, req));
    }

    @Test
    void submitReport_ThresholdReached_AutoHidesProfile() {
        Profile reporterProfile = new Profile();
        reporterProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(reporterProfile));

        Profile reportedProfile = new Profile();
        reportedProfile.setId(20L);
        reportedProfile.setUserId(2L);
        reportedProfile.setVisibility(Visibility.VISIBLE);
        when(profileRepository.findById(20L)).thenReturn(Optional.of(reportedProfile));

        when(reportRepository.existsByReporterProfileIdAndReportedProfileId(10L, 20L)).thenReturn(false);
        when(reportRepository.countByReportedProfileIdAndStatus(20L, ReportStatus.OPEN)).thenReturn(3L); // threshold 3 reached

        Report saved = new Report(10L, 20L, ReportReason.ASKED_FOR_MONEY, "Asked for cash");
        saved.setId(101L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);

        ReportSubmitRequest req = new ReportSubmitRequest();
        req.setReportedProfileId(20L);
        req.setReason(ReportReason.ASKED_FOR_MONEY);
        req.setDetails("Asked for cash");

        moderationService.submitReport(1L, req);

        assertEquals(Visibility.HIDDEN, reportedProfile.getVisibility());
        verify(profileRepository).save(reportedProfile);
    }

    @Test
    void resolveReport_Removed_SuspendsUserAndHidesProfile() {
        Report report = new Report(10L, 20L, ReportReason.ASKED_FOR_MONEY, "Scam");
        report.setId(50L);
        report.setStatus(ReportStatus.OPEN);
        when(reportRepository.findById(50L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile reportedProfile = new Profile();
        reportedProfile.setId(20L);
        reportedProfile.setUserId(2L);
        reportedProfile.setVisibility(Visibility.VISIBLE);
        when(profileRepository.findById(20L)).thenReturn(Optional.of(reportedProfile));

        User reportedUser = new User();
        reportedUser.setId(2L);
        reportedUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(reportedUser));

        ReportResponse resp = moderationService.resolveReport(50L, ReportStatus.REMOVED, 99L);

        assertEquals(ReportStatus.REMOVED, resp.getStatus());
        assertEquals(UserStatus.SUSPENDED, reportedUser.getStatus());
        assertTrue(reportedUser.isBlocked());
        assertEquals(Visibility.HIDDEN, reportedProfile.getVisibility());
    }
}
