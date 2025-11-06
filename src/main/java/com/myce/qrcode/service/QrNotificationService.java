package com.myce.qrcode.service;

import com.myce.reservation.entity.Reserver;

/**
 * QR 알림 전송 서비스
 * QR 발급/재발급 시 회원 및 비회원에게 알림을 전송합니다.
 */
public interface QrNotificationService {

    /**
     * QR 발급/재발급 시 알림을 전송합니다.
     * - 회원: 사이트 내 알림 + SSE 전송
     * - 비회원: 이메일 알림 전송
     *
     * @param reserver 예약자
     * @param isReissue 재발급 여부 (true: 재발급, false: 신규 발급)
     */
    void sendQrIssuedNotification(Reserver reserver, boolean isReissue);
}
