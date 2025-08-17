package com.myce.notification.entity.type;

public enum NotificationType {
    EXPO_REMINDER,      // 박람회 디데이 알림 -> 박람회 상세 페이지로 이동
    EVENT_REMINDER,     // 이벤트 시작 알림 -> 박람회 상세 페이지로 이동
    QR_ISSUED,          // QR 코드 발급 알림 -> 예매 상세 페이지로 이동
    RESERVATION_CONFIRM, // 예매 확정 알림 -> 예매 상세 페이지로 이동// 결제 완료 알림 -> 예매 상세 페이지로 이동
    EXPO_STATUS_CHANGE, // 박람회 상태 변경 알림 -> 박람회 상세 페이지로 이동
    AD_STATUS_CHANGE, // 광고 상태 변경 알림 -> 광고 상세 페이지로 이동
    GENERAL             // 일반 알림 -> 기본 동작
}