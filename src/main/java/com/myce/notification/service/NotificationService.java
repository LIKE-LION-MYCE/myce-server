package com.myce.notification.service;

import com.myce.notification.document.Notification;

public interface NotificationService {
    void saveNotification(Long memberId, Long expoId, String title, String content);
    void saveQrIssuedNotification(Long memberId, Long expoId, String expoTitle, boolean isReissue);
}