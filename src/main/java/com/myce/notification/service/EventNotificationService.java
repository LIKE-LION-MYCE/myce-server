package com.myce.notification.service;

/**
 * 이벤트 알림 서비스
 * 이벤트 시작 1시간 전에 예약자들에게 알림을 전송합니다.
 */
public interface EventNotificationService {
    
    /**
     * 1시간 후 시작하는 이벤트들을 찾아서 알림을 전송합니다.
     * 중복 알림을 방지하기 위해 이미 알림이 전송된 이벤트는 제외합니다.
     */
    void sendUpcomingEventNotifications();
}