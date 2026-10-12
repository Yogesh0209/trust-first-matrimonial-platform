package com.tfmsp.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.OtpToken;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.OtpPurpose;
import com.tfmsp.backend.repository.OtpTokenRepository;
import com.tfmsp.backend.repository.UserRepository;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OtpService(OtpTokenRepository otpTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.otpTokenRepository = otpTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Generates a 6-digit OTP, saves the BCrypt hash, and returns the raw OTP (to
     * send via email/SMS).
     */
    @Transactional
    public String generateAndSendOtp(User user, OtpPurpose purpose) {
        // FRD Section 5: Max 3 resends per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long sentInLastHour = otpTokenRepository.countByUserIdAndPurposeAndCreatedAtAfter(user.getId(), purpose,
                oneHourAgo);
        if (sentInLastHour >= 3) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many OTP requests. Please try again after an hour.");
        }

        // FRD Section 5: Resend cooldown of 60 seconds
        otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(), purpose)
                .ifPresent(latest -> {
                    if (latest.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                                "Please wait at least 60 seconds before requesting another OTP.");
                    }
                });

        // Generate 6-digit number
        String rawOtp = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpToken token = new OtpToken();
        token.setUserId(user.getId());
        token.setPurpose(purpose);
        token.setCodeHash(passwordEncoder.encode(rawOtp));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // FRD: 5 minutes
        token.setUsed(false);

        otpTokenRepository.save(token);

        // In production, integrate email service (JavaMailSender) or SMS gateway
        // For development / phone OTP (FRD: phone OTP mocked):
        log.info(">> [OTP GENERATED] User: {}, Purpose: {}, Code: {}", user.getEmail(), purpose, rawOtp);

        return rawOtp;
    }

    @Transactional
    public void verifyEmail(String email, String rawOtp) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isEmailVerified()) {
            return; // Already verified
        }

        verifyOtp(user.getId(), OtpPurpose.EMAIL_VERIFY, rawOtp);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void verifyPhone(String phone, String rawOtp) {
        User user = userRepository.findByPhone(phone.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isPhoneVerified()) {
            return; // Already verified
        }

        verifyOtp(user.getId(), OtpPurpose.PHONE_VERIFY, rawOtp);
        user.setPhoneVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public String resendOtp(String identifier, OtpPurpose purpose) {
        User user;
        if (purpose == OtpPurpose.EMAIL_VERIFY) {
            user = userRepository.findByEmail(identifier.trim().toLowerCase())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            if (user.isEmailVerified()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already verified");
            }
        } else if (purpose == OtpPurpose.PHONE_VERIFY) {
            user = userRepository.findByPhone(identifier.trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            if (user.isPhoneVerified()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is already verified");
            }
        } else {
            user = findUserByIdentifier(identifier);
        }

        return generateAndSendOtp(user, purpose);
    }

    @Transactional
    public void resetPassword(String identifier, String rawOtp, String newPassword) {
        User user = findUserByIdentifier(identifier);

        verifyOtp(user.getId(), OtpPurpose.PASSWORD_RESET, rawOtp);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    private User findUserByIdentifier(String identifier) {
        String trimmed = identifier.trim();
        return userRepository.findByEmail(trimmed.toLowerCase())
                .or(() -> userRepository.findByPhone(trimmed))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void verifyOtp(Long userId, OtpPurpose purpose, String rawOtp) {
        OtpToken token = otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(userId, purpose)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active OTP found. Please request a new one."));

        if (token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(rawOtp, token.getCodeHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP code");
        }

        token.setUsed(true);
        otpTokenRepository.save(token);
    }
}
