package com.myce.qrcode.service.impl;

import com.myce.notification.service.NotificationService;
import com.myce.notification.service.SupportEmailService;
import com.myce.qrcode.service.QrNotificationService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.entity.code.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * QR 알림 전송 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrNotificationServiceImpl implements QrNotificationService {

    private final NotificationService notificationService;
    private final SupportEmailService supportEmailService;

    @Override
    public void sendQrIssuedNotification(Reserver reserver, boolean isReissue) {
        try {
            Reservation reservation = reserver.getReservation();
            String expoTitle = reservation.getExpo().getTitle();

            if (reservation.getUserType() == UserType.MEMBER) {
                // 회원: 사이트 내 알림 + SSE 전송
                sendMemberNotification(reserver, reservation, expoTitle, isReissue);
            } else {
                // 비회원: 이메일 알림
                sendGuestEmailNotification(reserver, reservation, expoTitle, isReissue);
            }
        } catch (Exception e) {
            log.error("QR 발급 알림 처리 실패 - 예약자 ID: {}, 오류: {}",
                    reserver.getId(), e.getMessage(), e);
        }
    }

    /**
     * 회원에게 사이트 내 알림 + SSE 전송
     */
    private void sendMemberNotification(Reserver reserver, Reservation reservation,
                                        String expoTitle, boolean isReissue) {
        Long memberId = reservation.getUserId();
        notificationService.sendQrIssuedNotification(memberId, reservation.getId(), expoTitle, isReissue);
        log.info("회원 QR {} 알림 처리 완료 - 예약자 ID: {}, 회원 ID: {}",
                isReissue ? "재발급" : "발급", reserver.getId(), memberId);
    }

    /**
     * 비회원에게 이메일 알림 전송
     */
    private void sendGuestEmailNotification(Reserver reserver, Reservation reservation,
                                           String expoTitle, boolean isReissue) {
        String subject = String.format("[박람회 QR 코드 %s] %s",
                isReissue ? "재발급" : "발급", expoTitle);

        String body = String.format(
                "안녕하세요 %s님,<br><br>" +
                        "박람회 '%s'의 QR 코드가 %s되었습니다.<br><br>" +
                        "[예매 정보]<br>" +
                        "- 예약자: %s<br>" +
                        "- 예약번호: %s<br>" +
                        "QR 코드는 박람회 당일 입장 시 필요합니다.<br>" +
                        "예매 상세 조회: <a href=\"https://myce.live/guest-reservation\">바로가기</a><br><br>" +
                        "감사합니다.",
                reserver.getName(),
                expoTitle,
                isReissue ? "재발급" : "발급",
                reserver.getName(),
                reservation.getReservationCode()
        );

        supportEmailService.sendSupportMail(reserver.getEmail(), subject, body);
        log.info("비회원 QR {} 이메일 알림 전송 완료 - 예약자 ID: {}, 이메일: {}",
                isReissue ? "재발급" : "발급", reserver.getId(), reserver.getEmail());
    }
}
