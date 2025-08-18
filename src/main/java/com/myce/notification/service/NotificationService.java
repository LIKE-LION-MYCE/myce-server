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
    void sendQrIssuedNotificationByReservationId(Long reservationId);
    void sendExpoStartNotification(Long expoId);
    void sendEventHourReminderNotification(Long expoId, String eventName, String startTime);
    void sendPaymentCompleteNotification(Long memberId, Long reservationId, String expoTitle, String paymentAmount);
    void sendExpoStatusChangeNotification(Long expoId, String expoTitle, String oldStatus, String newStatus);
    void sendAdvertisementStatusChangeNotification(Long advertisementId, String adTitle, String oldStatus, String newStatus);
    List<NotificationResponse> getNotificationsByMemberId(Long memberId);
    void markAsRead(String notificationId, Long memberId);
    void markAllAsRead(Long memberId);
}