package com.tfmsp.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfmsp.backend.entity.Verification;
import com.tfmsp.backend.enums.ReviewStatus;
import com.tfmsp.backend.enums.VerificationType;

public interface VerificationRepository extends JpaRepository<Verification, Long> {

    List<Verification> findByProfileIdOrderBySubmittedAtDesc(Long profileId);

    Optional<Verification> findTopByProfileIdAndTypeOrderBySubmittedAtDesc(Long profileId, VerificationType type);

    long countByProfileIdAndTypeAndSubmittedAtAfter(Long profileId, VerificationType type, LocalDateTime after);

    List<Verification> findByStatusOrderBySubmittedAtAsc(ReviewStatus status);
}
