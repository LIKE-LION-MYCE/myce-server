package com.myce.notification.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.notification.dto.NotificationResponse;
import com.myce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        List<NotificationResponse> notifications = notificationService.getNotificationsByMemberId(memberId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String notificationId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        notificationService.markAsRead(notificationId, memberId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.ok().build();
    }
}