package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.OtpToken;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.OtpPurpose;
import com.tfmsp.backend.repository.OtpTokenRepository;
import com.tfmsp.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(otpTokenRepository, userRepository, passwordEncoder);
    }

    private User createSampleUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPhone("9876543210");
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        return user;
    }

    @Test
    void generateAndSendOtp_Success() {
        User user = createSampleUser();
        when(otpTokenRepository.countByUserIdAndPurposeAndCreatedAtAfter(eq(1L), eq(OtpPurpose.EMAIL_VERIFY), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(1L, OtpPurpose.EMAIL_VERIFY))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_otp");

        String rawOtp = otpService.generateAndSendOtp(user, OtpPurpose.EMAIL_VERIFY);

        assertNotNull(rawOtp);
        assertEquals(6, rawOtp.length());
        ArgumentCaptor<OtpToken> captor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpTokenRepository).save(captor.capture());
        OtpToken saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(OtpPurpose.EMAIL_VERIFY, saved.getPurpose());
        assertEquals("hashed_otp", saved.getCodeHash());
        assertFalse(saved.isUsed());
    }

    @Test
    void generateAndSendOtp_RateLimitExceeded_ThrowsException() {
        User user = createSampleUser();
        when(otpTokenRepository.countByUserIdAndPurposeAndCreatedAtAfter(eq(1L), eq(OtpPurpose.EMAIL_VERIFY), any(LocalDateTime.class)))
                .thenReturn(3L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> otpService.generateAndSendOtp(user, OtpPurpose.EMAIL_VERIFY));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Too many OTP requests"));
        verify(otpTokenRepository, never()).save(any());
    }

    @Test
    void generateAndSendOtp_CooldownActive_ThrowsException() {
        User user = createSampleUser();
        when(otpTokenRepository.countByUserIdAndPurposeAndCreatedAtAfter(eq(1L), eq(OtpPurpose.EMAIL_VERIFY), any(LocalDateTime.class)))
                .thenReturn(1L);

        OtpToken recentToken = new OtpToken();
        recentToken.setCreatedAt(LocalDateTime.now().minusSeconds(30)); // 30 seconds ago (< 60s)
        when(otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(1L, OtpPurpose.EMAIL_VERIFY))
                .thenReturn(Optional.of(recentToken));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> otpService.generateAndSendOtp(user, OtpPurpose.EMAIL_VERIFY));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        assertTrue(ex.getReason().contains("wait at least 60 seconds"));
    }

    @Test
    void verifyEmail_Success() {
        User user = createSampleUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        OtpToken token = new OtpToken();
        token.setUserId(1L);
        token.setPurpose(OtpPurpose.EMAIL_VERIFY);
        token.setCodeHash("hashed_otp");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);

        when(otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(1L, OtpPurpose.EMAIL_VERIFY))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashed_otp")).thenReturn(true);

        otpService.verifyEmail("user@example.com", "123456");

        assertTrue(user.isEmailVerified());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(otpTokenRepository).save(token);
    }

    @Test
    void verifyEmail_AlreadyVerified_NoOp() {
        User user = createSampleUser();
        user.setEmailVerified(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        otpService.verifyEmail("user@example.com", "123456");

        verify(otpTokenRepository, never()).findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(any(), any());
        verify(userRepository, never()).save(user);
    }

    @Test
    void verifyPhone_Success() {
        User user = createSampleUser();
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(user));

        OtpToken token = new OtpToken();
        token.setUserId(1L);
        token.setPurpose(OtpPurpose.PHONE_VERIFY);
        token.setCodeHash("hashed_otp");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);

        when(otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(1L, OtpPurpose.PHONE_VERIFY))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("654321", "hashed_otp")).thenReturn(true);

        otpService.verifyPhone("9876543210", "654321");

        assertTrue(user.isPhoneVerified());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(otpTokenRepository).save(token);
    }

    @Test
    void verifyPhone_UserNotFound_ThrowsNotFound() {
        when(userRepository.findByPhone("9999999999")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> otpService.verifyPhone("9999999999", "123456"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void verifyOtp_InvalidCode_ThrowsBadRequest() {
        User user = createSampleUser();
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(user));

        OtpToken token = new OtpToken();
        token.setUserId(1L);
        token.setPurpose(OtpPurpose.PHONE_VERIFY);
        token.setCodeHash("hashed_otp");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);

        when(otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(1L, OtpPurpose.PHONE_VERIFY))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("wrong_code", "hashed_otp")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> otpService.verifyPhone("9876543210", "wrong_code"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid OTP code"));
        assertEquals(1, token.getFailedAttempts());
        assertFalse(user.isPhoneVerified());
        assertFalse(token.isUsed());
    }

    @Test
    void verifyOtp_fiveFailedAttempts_invalidatesToken() {
        User user = createSampleUser();
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(user));

        OtpToken token = new OtpToken();
        token.setUserId(1L);
        token.setPurpose(OtpPurpose.PHONE_VERIFY);
        token.setCodeHash("hashed_otp");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);
        token.setFailedAttempts(4); // 4 previous failures

        when(otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(1L, OtpPurpose.PHONE_VERIFY))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("wrong_code", "hashed_otp")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> otpService.verifyPhone("9876543210", "wrong_code"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Too many wrong attempts"));
        assertEquals(5, token.getFailedAttempts());
        assertTrue(token.isUsed()); // Token is invalidated
    }

    @Test
    void resendOtp_EmailVerify_Success() {
        User user = createSampleUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(otpTokenRepository.countByUserIdAndPurposeAndCreatedAtAfter(eq(1L), eq(OtpPurpose.EMAIL_VERIFY), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_otp");

        String rawOtp = otpService.resendOtp("user@example.com", OtpPurpose.EMAIL_VERIFY);

        assertNotNull(rawOtp);
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void resendOtp_EmailAlreadyVerified_ThrowsBadRequest() {
        User user = createSampleUser();
        user.setEmailVerified(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> otpService.resendOtp("user@example.com", OtpPurpose.EMAIL_VERIFY));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Email is already verified"));
    }

    @Test
    void resendOtp_PhoneVerify_Success() {
        User user = createSampleUser();
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(user));
        when(otpTokenRepository.countByUserIdAndPurposeAndCreatedAtAfter(eq(1L), eq(OtpPurpose.PHONE_VERIFY), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_otp");

        String rawOtp = otpService.resendOtp("9876543210", OtpPurpose.PHONE_VERIFY);

        assertNotNull(rawOtp);
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void resendOtp_PhoneAlreadyVerified_ThrowsBadRequest() {
        User user = createSampleUser();
        user.setPhoneVerified(true);
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> otpService.resendOtp("9876543210", OtpPurpose.PHONE_VERIFY));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Phone is already verified"));
    }

    @Test
    void resetPassword_Success() {
        User user = createSampleUser();
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        OtpToken token = new OtpToken();
        token.setUserId(1L);
        token.setPurpose(OtpPurpose.PASSWORD_RESET);
        token.setCodeHash("hashed_otp");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);

        when(otpTokenRepository.findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(1L, OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.matches("123456", "hashed_otp")).thenReturn(true);
        when(passwordEncoder.encode("NewSecret@123")).thenReturn("new_hashed_password");

        otpService.resetPassword("user@example.com", "123456", "NewSecret@123");

        assertEquals("new_hashed_password", user.getPasswordHash());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(otpTokenRepository).save(token);
    }
}
