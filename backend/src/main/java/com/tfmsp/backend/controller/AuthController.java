package com.tfmsp.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tfmsp.backend.dto.AuthResponse;
import com.tfmsp.backend.dto.ForgotPasswordRequest;
import com.tfmsp.backend.dto.LoginRequest;
import com.tfmsp.backend.dto.RegisterRequest;
import com.tfmsp.backend.dto.ResendOtpRequest;
import com.tfmsp.backend.dto.ResetPasswordRequest;
import com.tfmsp.backend.dto.UserResponse;
import com.tfmsp.backend.dto.VerifyOtpRequest;
import com.tfmsp.backend.enums.OtpPurpose;
import com.tfmsp.backend.service.AuthService;
import com.tfmsp.backend.service.OtpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyEmail(request.getIdentifier(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<Map<String, String>> verifyPhone(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyPhone(request.getIdentifier(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Phone verified successfully"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        otpService.resendOtp(request.getIdentifier(), request.getPurpose());
        return ResponseEntity.ok(Map.of("message", "OTP resent successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        otpService.resendOtp(request.getIdentifier(), OtpPurpose.PASSWORD_RESET);
        return ResponseEntity.ok(Map.of("message", "Password reset OTP sent successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        otpService.resetPassword(request.getIdentifier(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
