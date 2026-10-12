package com.tfmsp.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfmsp.backend.dto.PhotoUploadRequest;
import com.tfmsp.backend.dto.PrivacyUpdateRequest;
import com.tfmsp.backend.dto.ProfilePhotoResponse;
import com.tfmsp.backend.dto.ProfileRequest;
import com.tfmsp.backend.dto.ProfileResponse;
import com.tfmsp.backend.enums.MaritalStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.exception.GlobalExceptionHandler;
import com.tfmsp.backend.service.ProfileService;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileService profileService;

    @Mock
    private com.tfmsp.backend.service.FreshnessService freshnessService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ProfileController controller = new ProfileController(profileService, freshnessService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyProfile_Success() throws Exception {
        ProfileResponse response = new ProfileResponse();
        response.setId(10L);
        response.setFullName("Aarav Sharma");
        response.setCity("Pune");
        response.setCompleteness(100);

        when(profileService.getOwnProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/profiles/me")
                .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.city").value("Pune"))
                .andExpect(jsonPath("$.completeness").value(100));
    }

    @Test
    void updateMyProfile_Success() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setMaritalStatus(MaritalStatus.NEVER_MARRIED);
        request.setMotherTongue("Marathi");
        request.setCity("Pune");
        request.setEducation("BE");
        request.setOccupation("Engineer");

        ProfileResponse response = new ProfileResponse();
        response.setId(10L);
        response.setCity("Pune");

        when(profileService.createOrUpdateOwnProfile(eq(1L), any(ProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/profiles/me")
                .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Pune"));
    }

    @Test
    void updateMyProfile_ValidationFailure() throws Exception {
        ProfileRequest request = new ProfileRequest();
        // Missing required fields (maritalStatus, city, etc.)

        mockMvc.perform(put("/api/profiles/me")
                .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getProfileById_Success() throws Exception {
        ProfileResponse response = new ProfileResponse();
        response.setId(25L);
        response.setCity("Bangalore");
        response.setTrustBand("HIGH");

        when(profileService.getPublicProfile(25L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/profiles/25")
                .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trustBand").value("HIGH"));
    }

    @Test
    void addPhoto_Success() throws Exception {
        PhotoUploadRequest request = new PhotoUploadRequest();
        request.setPhotoUrl("https://example.com/photo.jpg");
        request.setPrimary(true);

        ProfilePhotoResponse response = new ProfilePhotoResponse(1L, "https://example.com/photo.jpg", true, LocalDateTime.now());

        when(profileService.addPhoto(eq(1L), any(PhotoUploadRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/profiles/me/photos")
                .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.primary").value(true));
    }

    @Test
    void setPrimaryPhoto_Success() throws Exception {
        mockMvc.perform(put("/api/profiles/me/photos/5/primary")
                .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Primary photo updated successfully"));

        verify(profileService).setPrimaryPhoto(1L, 5L);
    }

    @Test
    void deletePhoto_Success() throws Exception {
        mockMvc.perform(delete("/api/profiles/me/photos/5")
                .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Photo deleted successfully"));

        verify(profileService).deletePhoto(1L, 5L);
    }

    @Test
    void updatePrivacy_Success() throws Exception {
        PrivacyUpdateRequest request = new PrivacyUpdateRequest();
        request.setVisibility(Visibility.HIDDEN);

        mockMvc.perform(put("/api/profiles/me/privacy")
                .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Privacy settings updated successfully"));

        verify(profileService).updatePrivacy(eq(1L), any(PrivacyUpdateRequest.class));
    }

    @Test
    void confirmActive_Success() throws Exception {
        mockMvc.perform(post("/api/profiles/me/confirm-active")
                .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Active status confirmed and profile freshness renewed"));

        verify(freshnessService).confirmActive(1L);
    }
}
