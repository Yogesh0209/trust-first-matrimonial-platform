package com.tfmsp.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.ConversationResponse;
import com.tfmsp.backend.dto.MessageResponse;
import com.tfmsp.backend.dto.SendMessageRequest;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.InteractionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final InteractionService interactionService;
    private final UserRepository userRepository;

    public ConversationController(InteractionService interactionService, UserRepository userRepository) {
        this.interactionService = interactionService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getConversations(Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(interactionService.getConversations(callerUserId));
    }

    @GetMapping("/{interestId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long interestId,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        return ResponseEntity.ok(interactionService.getMessages(callerUserId, interestId));
    }

    @PostMapping("/{interestId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long interestId,
            @Valid @RequestBody SendMessageRequest req,
            Authentication auth) {
        Long callerUserId = getAuthenticatedUserId(auth);
        MessageResponse response = interactionService.sendMessage(callerUserId, interestId, req.getBody());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
