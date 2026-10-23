package com.tfmsp.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tfmsp.backend.dto.NotificationResponse;
import com.tfmsp.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(Authentication auth) {
        return ResponseEntity.ok(notificationService.list(Long.valueOf(auth.getName())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication auth) {
        return ResponseEntity.ok(Map.of("unread", notificationService.unreadCount(Long.valueOf(auth.getName()))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable Long id, Authentication auth) {
        notificationService.markRead(Long.valueOf(auth.getName()), id);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(Authentication auth) {
        int count = notificationService.markAllRead(Long.valueOf(auth.getName()));
        return ResponseEntity.ok(Map.of("message", "All marked as read", "updated", count));
    }
}