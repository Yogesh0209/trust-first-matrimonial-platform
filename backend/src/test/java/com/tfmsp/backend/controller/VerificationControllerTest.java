package com.tfmsp.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfmsp.backend.dto.GovIdSubmissionRequest;
import com.tfmsp.backend.dto.SelfieSubmissionRequest;
import com.tfmsp.backend.dto.VerificationResponse;
import com.tfmsp.backend.dto.VerificationReviewRequest;
import com.tfmsp.backend.enums.ReviewStatus;
import com.tfmsp.backend.enums.VerificationType;
import com.tfmsp.backend.exception.GlobalExceptionHandler;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.PlanLimitService;
import com.tfmsp.backend.service.VerificationService;

@ExtendWith(MockitoExtension.class)
class VerificationControllerTest {

    private MockMvc candidateMockMvc;
    private MockMvc hostMockMvc;
    @Mock private UserRepository userRepository;
    @Mock private PlanLimitService planLimitService;

    @Mock
    private VerificationService verificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        VerificationController candidateController = new VerificationController(verificationService, userRepository, planLimitService);
        HostVerificationController hostController = new HostVerificationController(verificationService);

        candidateMockMvc = MockMvcBuilders.standaloneSetup(candidateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        hostMockMvc = MockMvcBuilders.standaloneSetup(hostController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void submitSelfie_Success() throws Exception {
        SelfieSubmissionRequest request = new SelfieSubmissionRequest();
        request.setFileUrl("https://example.com/selfie.jpg");

        VerificationResponse response = new VerificationResponse();
        response.setId(10L);
        response.setType(VerificationType.SELFIE);
        response.setStatus(ReviewStatus.PENDING);

        when(verificationService.submitSelfie(eq(1L), any(SelfieSubmissionRequest.class))).thenReturn(response);

        candidateMockMvc.perform(post("/api/verification/selfie")
                .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("SELFIE"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void submitGovId_Success() throws Exception {
        GovIdSubmissionRequest request = new GovIdSubmissionRequest();
        request.setFileUrl("https://example.com/id.jpg");
        request.setDocumentType("AADHAAR");
        request.setMaskedReference("1234");

        VerificationResponse response = new VerificationResponse();
        response.setId(11L);
        response.setType(VerificationType.GOV_ID);
        response.setStatus(ReviewStatus.PENDING);

        when(verificationService.submitGovId(eq(1L), any(GovIdSubmissionRequest.class))).thenReturn(response);

        candidateMockMvc.perform(post("/api/verification/id")
                .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("GOV_ID"));
    }

    @Test
    void getReviewQueue_Success() throws Exception {
        VerificationResponse response = new VerificationResponse();
        response.setId(10L);
        response.setType(VerificationType.SELFIE);
        response.setStatus(ReviewStatus.PENDING);

        when(verificationService.getReviewQueue(1L)).thenReturn(List.of(response));

        hostMockMvc.perform(get("/api/host/verifications")
                .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    void reviewVerification_Success() throws Exception {
        VerificationReviewRequest request = new VerificationReviewRequest();
        request.setDecision(ReviewStatus.APPROVED);

        VerificationResponse response = new VerificationResponse();
        response.setId(10L);
        response.setStatus(ReviewStatus.APPROVED);

        when(verificationService.reviewVerification(eq(1L), eq(10L), any(VerificationReviewRequest.class)))
                .thenReturn(response);

        hostMockMvc.perform(put("/api/host/verifications/10")
                .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
