package com.myce.notification.entity.type;

public enum NotificationTargetType {
    QR_ISSUED,
    EXPO,           // 박람회 상세 페이지로 이동
    RESERVATION,    // 예매 상세 페이지로 이동     // 이벤트 관련 -> 박람회 상세 페이지로 이동     // 일반 알림 -> 기본 동작
}