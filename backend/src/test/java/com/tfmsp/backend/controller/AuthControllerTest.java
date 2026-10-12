package com.tfmsp.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfmsp.backend.dto.AuthResponse;
import com.tfmsp.backend.dto.ForgotPasswordRequest;
import com.tfmsp.backend.dto.LoginRequest;
import com.tfmsp.backend.dto.ResendOtpRequest;
import com.tfmsp.backend.dto.ResetPasswordRequest;
import com.tfmsp.backend.dto.UserResponse;
import com.tfmsp.backend.dto.VerifyOtpRequest;
import com.tfmsp.backend.enums.OtpPurpose;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.exception.GlobalExceptionHandler;
import com.tfmsp.backend.service.AuthService;
import com.tfmsp.backend.service.OtpService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private OtpService otpService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService, otpService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password@123");

        AuthResponse authResponse = new AuthResponse("mock_jwt_token", Role.END_USER, 10L, 3600000L);
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock_jwt_token"))
                .andExpect(jsonPath("$.role").value("END_USER"));
    }

    @Test
    void verifyEmail_Success() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setIdentifier("user@example.com");
        request.setOtp("123456");

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    void verifyPhone_Success() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setIdentifier("9876543210");
        request.setOtp("123456");

        mockMvc.perform(post("/api/auth/verify-phone")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Phone verified successfully"));
    }

    @Test
    void resendOtp_Success() throws Exception {
        ResendOtpRequest request = new ResendOtpRequest();
        request.setIdentifier("user@example.com");
        request.setPurpose(OtpPurpose.EMAIL_VERIFY);

        mockMvc.perform(post("/api/auth/resend-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP resent successfully"));
    }

    @Test
    void forgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setIdentifier("user@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset OTP sent successfully"));
    }

    @Test
    void resetPassword_Success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setIdentifier("user@example.com");
        request.setOtp("123456");
        request.setNewPassword("NewPassword@123");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void verifyEmail_ValidationFailure() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        // Missing fields

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
