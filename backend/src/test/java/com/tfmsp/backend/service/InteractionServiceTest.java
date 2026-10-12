package com.tfmsp.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.InterestResponse;
import com.tfmsp.backend.dto.MessageResponse;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.InterestStatus;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private InterestRepository interestRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfilePhotoRepository profilePhotoRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileService profileService;

    private InteractionService interactionService;

    @BeforeEach
    void setUp() {
        interactionService = new InteractionService(
                interestRepository,
                messageRepository,
                profileRepository,
                profilePhotoRepository,
                userRepository,
                profileService
        );
    }

    @Test
    void sendInterest_Success() {
        Profile senderProfile = new Profile();
        senderProfile.setId(10L);
        senderProfile.setTrustLevel((short) 1);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(senderProfile));

        Profile receiverProfile = new Profile();
        receiverProfile.setId(20L);
        receiverProfile.setUserId(2L);
        receiverProfile.setVisibility(Visibility.VISIBLE);
        when(profileRepository.findById(20L)).thenReturn(Optional.of(receiverProfile));

        User receiverUser = new User();
        receiverUser.setId(2L);
        receiverUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiverUser));

        when(interestRepository.existsBySenderProfileIdAndReceiverProfileId(10L, 20L)).thenReturn(false);
        when(interestRepository.existsBySenderProfileIdAndReceiverProfileId(20L, 10L)).thenReturn(false);
        when(interestRepository.countBySenderProfileIdAndCreatedAtAfter(eq(10L), any(LocalDateTime.class))).thenReturn(2L);

        Interest saved = new Interest(10L, 20L);
        saved.setId(100L);
        when(interestRepository.save(any(Interest.class))).thenReturn(saved);

        InterestResponse resp = interactionService.sendInterest(1L, 20L);

        assertNotNull(resp);
        assertEquals(InterestStatus.PENDING, resp.getStatus());
        assertEquals(10L, resp.getSenderProfileId());
        assertEquals(20L, resp.getReceiverProfileId());
    }

    @Test
    void sendInterest_SelfInterest_ThrowsBadRequest() {
        Profile senderProfile = new Profile();
        senderProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(senderProfile));

        assertThrows(ResponseStatusException.class, () -> interactionService.sendInterest(1L, 10L));
    }

    @Test
    void sendInterest_RateLimitExceeded_ThrowsTooManyRequests() {
        Profile senderProfile = new Profile();
        senderProfile.setId(10L);
        senderProfile.setTrustLevel((short) 1); // limit is 5
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(senderProfile));

        Profile receiverProfile = new Profile();
        receiverProfile.setId(20L);
        receiverProfile.setUserId(2L);
        receiverProfile.setVisibility(Visibility.VISIBLE);
        when(profileRepository.findById(20L)).thenReturn(Optional.of(receiverProfile));

        User receiverUser = new User();
        receiverUser.setId(2L);
        receiverUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiverUser));

        when(interestRepository.countBySenderProfileIdAndCreatedAtAfter(eq(10L), any(LocalDateTime.class))).thenReturn(5L);

        assertThrows(ResponseStatusException.class, () -> interactionService.sendInterest(1L, 20L));
    }

    @Test
    void respondToInterest_Accept_Success() {
        Profile receiverProfile = new Profile();
        receiverProfile.setId(20L);
        when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(receiverProfile));

        Interest interest = new Interest(10L, 20L);
        interest.setId(50L);
        interest.setStatus(InterestStatus.PENDING);
        when(interestRepository.findById(50L)).thenReturn(Optional.of(interest));
        when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InterestResponse resp = interactionService.respondToInterest(2L, 50L, "ACCEPT");

        assertEquals(InterestStatus.ACCEPTED, resp.getStatus());
        assertNotNull(resp.getRespondedAt());
    }

    @Test
    void sendMessage_WhenInterestAccepted_Success() {
        Profile senderProfile = new Profile();
        senderProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(senderProfile));

        Interest interest = new Interest(10L, 20L);
        interest.setId(50L);
        interest.setStatus(InterestStatus.ACCEPTED);
        when(interestRepository.findById(50L)).thenReturn(Optional.of(interest));

        Message savedMsg = new Message(50L, 10L, "Hello, nice to connect with you!", false);
        savedMsg.setId(200L);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMsg);

        MessageResponse resp = interactionService.sendMessage(1L, 50L, "Hello, nice to connect with you!");

        assertNotNull(resp);
        assertEquals(50L, resp.getInterestId());
        assertEquals("Hello, nice to connect with you!", resp.getBody());
        assertFalse(resp.isFlagged());
    }

    @Test
    void sendMessage_FlaggedForSuspiciousFinancialRequest() {
        Profile senderProfile = new Profile();
        senderProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(senderProfile));

        Interest interest = new Interest(10L, 20L);
        interest.setId(50L);
        interest.setStatus(InterestStatus.ACCEPTED);
        when(interestRepository.findById(50L)).thenReturn(Optional.of(interest));

        Message savedMsg = new Message(50L, 10L, "Can you do gpay transfer of 5000 urgent?", true);
        savedMsg.setId(201L);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMsg);

        MessageResponse resp = interactionService.sendMessage(1L, 50L, "Can you do gpay transfer of 5000 urgent?");

        assertTrue(resp.isFlagged());
    }

    @Test
    void sendMessage_NotAccepted_ThrowsForbidden() {
        Profile senderProfile = new Profile();
        senderProfile.setId(10L);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(senderProfile));

        Interest interest = new Interest(10L, 20L);
        interest.setId(50L);
        interest.setStatus(InterestStatus.PENDING); // Not accepted yet!
        when(interestRepository.findById(50L)).thenReturn(Optional.of(interest));

        assertThrows(ResponseStatusException.class, () ->
                interactionService.sendMessage(1L, 50L, "Hello!"));
    }
}
