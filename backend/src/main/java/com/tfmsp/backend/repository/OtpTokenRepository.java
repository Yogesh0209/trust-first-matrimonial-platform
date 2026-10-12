package com.tfmsp.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.OtpToken;
import com.tfmsp.backend.enums.OtpPurpose;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // Find the latest active unused OTP for a user and purpose
    Optional<OtpToken> findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            Long userId, OtpPurpose purpose);

    // Count OTPs generated in the last hour (for rate limiting: max 3/hour)
    long countByUserIdAndPurposeAndCreatedAtAfter(
            Long userId, OtpPurpose purpose, LocalDateTime after);
}
