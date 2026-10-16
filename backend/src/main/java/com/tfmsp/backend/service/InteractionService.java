package com.tfmsp.backend.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.*;
import com.tfmsp.backend.entity.*;
import com.tfmsp.backend.enums.InterestStatus;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.enums.Visibility;
import com.tfmsp.backend.repository.*;

@Service
public class InteractionService {

    private final InterestRepository interestRepository;
    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final BlockRepository blockRepository;

    // Anti-scam pattern: phone numbers, URLs, suspicious financial solicitation
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,4}[-\\s]?)?\\(?\\d{3}\\)?[-\\s]?\\d{3}[-\\s]?\\d{4}|\\b\\d{10}\\b");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)\\b(https?://|www\\.)\\S+");
    private static final Pattern FINANCIAL_SCAM_PATTERN = Pattern.compile("(?i)\\b(gpay|phonepe|paytm|bank transfer|wire money|crypto|send cash|deposit money)\\b");

    public InteractionService(
            InterestRepository interestRepository,
            MessageRepository messageRepository,
            ProfileRepository profileRepository,
            ProfilePhotoRepository profilePhotoRepository,
            UserRepository userRepository,
            ProfileService profileService,
            BlockRepository blockRepository) {
        this.interestRepository = interestRepository;
        this.messageRepository = messageRepository;
        this.profileRepository = profileRepository;
        this.profilePhotoRepository = profilePhotoRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public InterestResponse sendInterest(Long senderUserId, Long receiverProfileId) {
        Profile senderProfile = profileRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender profile not found. Please complete profile first."));

        if (senderProfile.getId().equals(receiverProfileId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send interest to yourself");
        }

        Profile receiverProfile = profileRepository.findById(receiverProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target profile not found"));

        assertNotBlocked(senderProfile.getId(), receiverProfileId);
        
        if (receiverProfile.getVisibility() != Visibility.VISIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target profile is not currently visible");
        }

        User receiverUser = userRepository.findById(receiverProfile.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver user not found"));

        if (receiverUser.getStatus() != UserStatus.ACTIVE || receiverUser.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target member is not active");
        }

        // Check if interest already exists
        if (interestRepository.existsBySenderProfileIdAndReceiverProfileId(senderProfile.getId(), receiverProfileId) ||
            interestRepository.existsBySenderProfileIdAndReceiverProfileId(receiverProfileId, senderProfile.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An interest already exists between these profiles");
        }

        // Rate limit: 5/day for Level 0-1, 20/day for Level 2+
        int maxAllowedDaily = (senderProfile.getTrustLevel() >= 2) ? 20 : 5;
        long sentPast24h = interestRepository.countBySenderProfileIdAndCreatedAtAfter(
                senderProfile.getId(), LocalDateTime.now().minusHours(24));

        if (sentPast24h >= maxAllowedDaily) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Daily interest limit reached (" + maxAllowedDaily + " per 24 hours). Elevate verification level to unlock higher limits.");
        }

        Interest interest = new Interest(senderProfile.getId(), receiverProfileId);
        Interest saved = interestRepository.save(interest);

        User senderUser = userRepository.findById(senderUserId).orElse(null);
        return toInterestResponse(saved, senderUser, receiverUser, receiverProfile);
    }

    @Transactional
    public InterestResponse respondToInterest(Long receiverUserId, Long interestId, String action) {
        Profile receiverProfile = profileRepository.findByUserId(receiverUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver profile not found"));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interest not found"));

        if (!interest.getReceiverProfileId().equals(receiverProfile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: you are not the recipient of this interest");
        }

        if (interest.getStatus() != InterestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interest is not pending (current status: " + interest.getStatus() + ")");
        }

        if ("ACCEPT".equalsIgnoreCase(action)) {
            interest.setStatus(InterestStatus.ACCEPTED);
        } else if ("DECLINE".equalsIgnoreCase(action)) {
            interest.setStatus(InterestStatus.DECLINED);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action: must be ACCEPT or DECLINE");
        }

        interest.setRespondedAt(LocalDateTime.now());
        Interest saved = interestRepository.save(interest);

        Profile senderProfile = profileRepository.findById(saved.getSenderProfileId()).orElse(null);
        User senderUser = (senderProfile != null) ? userRepository.findById(senderProfile.getUserId()).orElse(null) : null;
        User receiverUser = userRepository.findById(receiverUserId).orElse(null);

        return toInterestResponse(saved, senderUser, receiverUser, senderProfile);
    }

    @Transactional
    public void withdrawInterest(Long senderUserId, Long interestId) {
        Profile senderProfile = profileRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender profile not found"));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interest not found"));

        if (!interest.getSenderProfileId().equals(senderProfile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: you did not send this interest");
        }

        if (interest.getStatus() == InterestStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot withdraw an already accepted interest. Chat is already active.");
        }

        interestRepository.delete(interest);
    }

    public List<InterestResponse> getSentInterests(Long senderUserId) {
        Profile senderProfile = profileRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender profile not found"));

        List<Interest> interests = interestRepository.findBySenderProfileIdOrderByCreatedAtDesc(senderProfile.getId());
        User senderUser = userRepository.findById(senderUserId).orElse(null);

        List<InterestResponse> responses = new ArrayList<>();
        for (Interest i : interests) {
            profileRepository.findById(i.getReceiverProfileId()).ifPresent(recProfile -> {
                User recUser = userRepository.findById(recProfile.getUserId()).orElse(null);
                responses.add(toInterestResponse(i, senderUser, recUser, recProfile));
            });
        }
        return responses;
    }

    public List<InterestResponse> getReceivedInterests(Long receiverUserId) {
        Profile receiverProfile = profileRepository.findByUserId(receiverUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver profile not found"));

        List<Interest> interests = interestRepository.findByReceiverProfileIdOrderByCreatedAtDesc(receiverProfile.getId());
        User recUser = userRepository.findById(receiverUserId).orElse(null);

        List<InterestResponse> responses = new ArrayList<>();
        for (Interest i : interests) {
            profileRepository.findById(i.getSenderProfileId()).ifPresent(sendProfile -> {
                User sendUser = userRepository.findById(sendProfile.getUserId()).orElse(null);
                responses.add(toInterestResponse(i, sendUser, recUser, sendProfile));
            });
        }
        return responses;
    }

    public List<ConversationResponse> getConversations(Long userId) {
        Profile callerProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        List<Interest> acceptedInterests = interestRepository.findAll().stream()
                .filter(i -> i.getStatus() == InterestStatus.ACCEPTED)
                .filter(i -> i.getSenderProfileId().equals(callerProfile.getId()) || i.getReceiverProfileId().equals(callerProfile.getId()))
                .collect(Collectors.toList());

        List<ConversationResponse> conversations = new ArrayList<>();

        for (Interest interest : acceptedInterests) {
            Long otherProfileId = interest.getSenderProfileId().equals(callerProfile.getId())
                    ? interest.getReceiverProfileId()
                    : interest.getSenderProfileId();

            profileRepository.findById(otherProfileId).ifPresent(otherProfile -> {
                userRepository.findById(otherProfile.getUserId()).ifPresent(otherUser -> {
                    ConversationResponse conv = new ConversationResponse();
                    conv.setInterestId(interest.getId());
                    conv.setOtherProfileId(otherProfile.getId());
                    conv.setOtherUserId(otherUser.getId());
                    conv.setOtherFullName(otherUser.getFullName());

                    int completeness = profileService.calculateCompleteness(otherProfile);
                    int score = profileService.calculateTrustScore(otherUser, otherProfile, completeness);
                    conv.setTrustScore(score);
                    conv.setTrustBand(profileService.getTrustBand(score));

                    profilePhotoRepository.findByProfileIdAndIsPrimaryTrue(otherProfile.getId())
                            .ifPresent(p -> conv.setOtherPhotoUrl(p.getPhotoUrl()));

                    messageRepository.findFirstByInterestIdOrderBySentAtDesc(interest.getId()).ifPresent(lastMsg -> {
                        conv.setLastMessage(lastMsg.getBody());
                        conv.setLastMessageTime(lastMsg.getSentAt());
                    });

                    conversations.add(conv);
                });
            });
        }

        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return conversations;
    }

    public List<MessageResponse> getMessages(Long userId, Long interestId) {
        Profile callerProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interest conversation not found"));

        if (!interest.getSenderProfileId().equals(callerProfile.getId()) &&
            !interest.getReceiverProfileId().equals(callerProfile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: you are not a participant in this conversation");
        }

        if (interest.getStatus() != InterestStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Messaging is only permitted after mutual interest is ACCEPTED");
        }

        List<Message> messages = messageRepository.findByInterestIdOrderBySentAtAsc(interestId);
        List<MessageResponse> responses = new ArrayList<>();

        for (Message m : messages) {
            MessageResponse res = new MessageResponse();
            res.setId(m.getId());
            res.setInterestId(m.getInterestId());
            res.setSenderProfileId(m.getSenderProfileId());
            res.setBody(m.getBody());
            res.setFlagged(m.isFlagged());
            res.setMine(m.getSenderProfileId().equals(callerProfile.getId()));
            res.setSentAt(m.getSentAt());
            responses.add(res);
        }

        return responses;
    }

    @Transactional
    public MessageResponse sendMessage(Long userId, Long interestId, String body) {
        Profile callerProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interest conversation not found"));

        if (!interest.getSenderProfileId().equals(callerProfile.getId()) &&
            !interest.getReceiverProfileId().equals(callerProfile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: you are not a participant in this conversation");
        }
        
        Long otherProfileId = interest.getSenderProfileId().equals(callerProfile.getId())
                ? interest.getReceiverProfileId() : interest.getSenderProfileId();
        assertNotBlocked(callerProfile.getId(), otherProfileId);

        if (interest.getStatus() != InterestStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Messaging is only enabled after interest is ACCEPTED");
        }

        // Anti-scam inspection
        boolean isFlagged = false;
        if (FINANCIAL_SCAM_PATTERN.matcher(body).find()) {
            isFlagged = true;
        }

        Message message = new Message(interestId, callerProfile.getId(), body.trim(), isFlagged);
        Message saved = messageRepository.save(message);

        // Update user's lastActiveAt freshness timestamp
        userRepository.findById(userId).ifPresent(u -> {
            u.setLastActiveAt(LocalDateTime.now());
            userRepository.save(u);
        });

        MessageResponse res = new MessageResponse();
        res.setId(saved.getId());
        res.setInterestId(saved.getInterestId());
        res.setSenderProfileId(saved.getSenderProfileId());
        res.setBody(saved.getBody());
        res.setFlagged(saved.isFlagged());
        res.setMine(true);
        res.setSentAt(saved.getSentAt());
        return res;
    }

    private InterestResponse toInterestResponse(
            Interest interest,
            User senderUser,
            User receiverUser,
            Profile targetProfile) {
        InterestResponse res = new InterestResponse();
        res.setId(interest.getId());
        res.setSenderProfileId(interest.getSenderProfileId());
        res.setReceiverProfileId(interest.getReceiverProfileId());
        res.setStatus(interest.getStatus());
        res.setCreatedAt(interest.getCreatedAt());
        res.setRespondedAt(interest.getRespondedAt());

        if (senderUser != null) {
            res.setSenderName(senderUser.getFullName());
        }
        if (receiverUser != null) {
            res.setReceiverName(receiverUser.getFullName());
        }

        if (targetProfile != null) {
            res.setCity(targetProfile.getCity());
            profilePhotoRepository.findByProfileIdAndIsPrimaryTrue(targetProfile.getId())
                    .ifPresent(p -> res.setPhotoUrl(p.getPhotoUrl()));
        }

        return res;
    }
    
    private void assertNotBlocked(Long profileA, Long profileB) {
        if (blockRepository.existsByBlockerProfileIdAndBlockedProfileId(profileA, profileB)
                || blockRepository.existsByBlockerProfileIdAndBlockedProfileId(profileB, profileA)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot interact with this profile");
        }
    }
}
