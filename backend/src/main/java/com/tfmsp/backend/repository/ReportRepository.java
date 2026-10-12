package com.tfmsp.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Report;
import com.tfmsp.backend.enums.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByReportedProfileId(Long reportedProfileId);

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findAllByOrderByCreatedAtDesc();

    long countByReportedProfileIdAndStatus(Long reportedProfileId, ReportStatus status);

    boolean existsByReporterProfileIdAndReportedProfileId(Long reporterProfileId, Long reportedProfileId);
}
