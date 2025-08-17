package com.myce.notification.service;

import com.myce.notification.document.Notification;
import com.myce.notification.dto.NotificationResponse;
import com.myce.notification.entity.type.NotificationTargetType;
import com.myce.notification.entity.type.NotificationType;

import java.util.List;

public interface NotificationService {
    void saveNotification(Long memberId, Long targetId, String title, String content,
                          NotificationType type, NotificationTargetType targetType);
    void sendQrIssuedNotification(Long memberId, Long reservationId, String expoTitle, boolean isReissue);
    void sendExpoStartNotification(Long expoId);
    List<NotificationResponse> getNotificationsByMemberId(Long memberId);
    void markAsRead(String notificationId, Long memberId);
    void markAllAsRead(Long memberId);
}