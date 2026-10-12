package com.tfmsp.backend.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.ReportResponse;
import com.tfmsp.backend.dto.ReportSubmitRequest;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.ReportStatus;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.*;

@Service
public class ModerationService {

    private final ReportRepository reportRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ModerationService(
            ReportRepository reportRepository,
            ProfileRepository profileRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.reportRepository = reportRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public ReportResponse submitReport(Long reporterUserId, ReportSubmitRequest req) {
        Profile reporterProfile = profileRepository.findByUserId(reporterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporter profile not found"));

        if (reporterProfile.getId().equals(req.getReportedProfileId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot report your own profile");
        }

        Profile reportedProfile = profileRepository.findById(req.getReportedProfileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reported profile not found"));

        if (reportRepository.existsByReporterProfileIdAndReportedProfileId(
                reporterProfile.getId(), req.getReportedProfileId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already submitted a report for this profile");
        }

        Report report = new Report(
                reporterProfile.getId(),
                req.getReportedProfileId(),
                req.getReason(),
                req.getDetails()
        );
        Report savedReport = reportRepository.save(report);

        // Anti-scam trigger: If 3 or more open reports exist against this profile, auto-hide for investigation
        long openReportsCount = reportRepository.countByReportedProfileIdAndStatus(req.getReportedProfileId(), ReportStatus.OPEN);
        if (openReportsCount >= 3) {
            reportedProfile.setVisibility(Visibility.HIDDEN);
            profileRepository.save(reportedProfile);

            auditLogRepository.save(new AuditLog(
                    null,
                    "AUTO_HIDE_SCAM_THRESHOLD",
                    "PROFILE",
                    reportedProfile.getId(),
                    "Profile auto-hidden due to " + openReportsCount + " open community reports"
            ));
        }

        auditLogRepository.save(new AuditLog(
                reporterUserId,
                "SUBMIT_REPORT",
                "REPORT",
                savedReport.getId(),
                "Reason: " + req.getReason()
        ));

        User reporterUser = userRepository.findById(reporterUserId).orElse(null);
        User reportedUser = userRepository.findById(reportedProfile.getUserId()).orElse(null);

        return toReportResponse(savedReport, reporterUser, reportedUser);
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId, ReportStatus resolution, Long moderatorUserId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        report.setStatus(resolution);
        report.setHandledBy(moderatorUserId);
        report.setResolvedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);

        Profile reportedProfile = profileRepository.findById(report.getReportedProfileId()).orElse(null);
        if (reportedProfile != null) {
            User reportedUser = userRepository.findById(reportedProfile.getUserId()).orElse(null);

            if (resolution == ReportStatus.REMOVED && reportedUser != null) {
                reportedUser.setStatus(UserStatus.SUSPENDED);
                reportedUser.setBlocked(true);
                userRepository.save(reportedUser);

                reportedProfile.setVisibility(Visibility.HIDDEN);
                profileRepository.save(reportedProfile);
            } else if (resolution == ReportStatus.DISMISSED) {
                // If no other open reports remain, restore profile visibility
                long remainingOpen = reportRepository.countByReportedProfileIdAndStatus(reportedProfile.getId(), ReportStatus.OPEN);
                if (remainingOpen == 0) {
                    reportedProfile.setVisibility(Visibility.VISIBLE);
                    profileRepository.save(reportedProfile);
                }
            }

            auditLogRepository.save(new AuditLog(
                    moderatorUserId,
                    "RESOLVE_REPORT",
                    "REPORT",
                    reportId,
                    "Resolution: " + resolution.name()
            ));

            Profile reporterProfile = profileRepository.findById(report.getReporterProfileId()).orElse(null);
            User reporterUser = (reporterProfile != null) ? userRepository.findById(reporterProfile.getUserId()).orElse(null) : null;

            return toReportResponse(saved, reporterUser, reportedUser);
        }

        return toReportResponse(saved, null, null);
    }

    public List<ReportResponse> getHostReports(Long bureauId) {
        List<User> bureauUsers = userRepository.findByBureauIdAndRole(bureauId, com.tfmsp.backend.enums.Role.END_USER);
        Set<Long> bureauUserIds = bureauUsers.stream().map(User::getId).collect(Collectors.toSet());

        List<Profile> bureauProfiles = profileRepository.findAll().stream()
                .filter(p -> bureauUserIds.contains(p.getUserId()))
                .collect(Collectors.toList());
        Set<Long> bureauProfileIds = bureauProfiles.stream().map(Profile::getId).collect(Collectors.toSet());

        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> bureauProfileIds.contains(r.getReportedProfileId()))
                .map(this::enrichReport)
                .collect(Collectors.toList());
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::enrichReport)
                .collect(Collectors.toList());
    }

    private ReportResponse enrichReport(Report report) {
        Profile reporterProfile = profileRepository.findById(report.getReporterProfileId()).orElse(null);
        User reporterUser = (reporterProfile != null) ? userRepository.findById(reporterProfile.getUserId()).orElse(null) : null;

        Profile reportedProfile = profileRepository.findById(report.getReportedProfileId()).orElse(null);
        User reportedUser = (reportedProfile != null) ? userRepository.findById(reportedProfile.getUserId()).orElse(null) : null;

        return toReportResponse(report, reporterUser, reportedUser);
    }

    private ReportResponse toReportResponse(Report report, User reporter, User reported) {
        ReportResponse res = new ReportResponse();
        res.setId(report.getId());
        res.setReporterProfileId(report.getReporterProfileId());
        res.setReportedProfileId(report.getReportedProfileId());
        res.setReason(report.getReason());
        res.setDetails(report.getDetails());
        res.setStatus(report.getStatus());
        res.setHandledBy(report.getHandledBy());
        res.setCreatedAt(report.getCreatedAt());
        res.setResolvedAt(report.getResolvedAt());

        if (reporter != null) {
            res.setReporterName(reporter.getFullName());
        }
        if (reported != null) {
            res.setReportedName(reported.getFullName());
        }

        return res;
    }
}
