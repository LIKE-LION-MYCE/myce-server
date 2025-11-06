package com.myce.payment.service.impl;

import com.myce.member.dto.MileageUpdateRequest;
import com.myce.member.service.MemberGradeService;
import com.myce.member.service.MemberMileageService;
import com.myce.notification.service.EmailSendService;
import com.myce.notification.service.NotificationService;
import com.myce.qrcode.service.QrCodeService;
import com.myce.reservation.dto.ReserverBulkSaveRequest;
import com.myce.reservation.entity.Reservation;
import com.myce.system.dto.message.MessageTemplate;
import com.myce.system.service.message.GenerateMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommonService {

    private static final String PAY_AMOUNT_MESSAGE_FORMAT = "%,d원";

    private final MemberMileageService memberMileageService;
    private final MemberGradeService memberGradeService;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;
    private final EmailSendService emailSendService;
    private final GenerateMessageService generateMessageService;

    // 마일리지 처리
    public void processMileage(int usedMileage, int savedMileage, long userId)  {
        MileageUpdateRequest mileageRequest = new MileageUpdateRequest(usedMileage, savedMileage);

        if (usedMileage > 0 || savedMileage > 0) {
            memberMileageService.updateMileageForReservation(userId, mileageRequest);
            log.info("마일리지 처리 완료 - 회원ID: {}, 사용: {}, 적립: {}",
                    userId, usedMileage, savedMileage);
        }

        // 회원 등급 업데이트
        memberGradeService.udpateGrade(userId);
    }

    // QR 생성
    public void issueQrForReservation(Reservation reservation) {
        try {
            qrCodeService.issueQrForReservation(reservation.getId());
            log.info("QR 코드 생성 완료 - reservationId: {}", reservation.getId());
        } catch (Exception qrError) {
            log.warn("QR 코드 생성 실패 (스케줄러에서 재시도) - reservationId: {}, 오류: {}",
                    reservation.getId(), qrError.getMessage());
        }
    }

    // 알림 전송
    public void sendAlert(Reservation reservation, int paidAmount) {
        long reservationId = reservation.getId();
        long userId = reservation.getUserId();
        String expoTitle = reservation.getExpo().getTitle();

        try {
            String payAmountMessage = PAY_AMOUNT_MESSAGE_FORMAT.formatted(paidAmount);
            notificationService.sendPaymentCompleteNotification(userId, reservationId, expoTitle, payAmountMessage);
            log.info("가상계좌 결제 완료 알림 발송 - 예약 ID: {}, 회원 ID: {}, 금액: {}",
                                                        reservationId, userId, payAmountMessage);
        } catch (Exception e) {
            log.error("가상계좌 결제 완료 알림 발송 실패 - 예약 ID: {}", reservationId, e);
        }
    }

    // 이메일 전송
    public void sendEmail(Reservation reservation, ReserverBulkSaveRequest.ReserverSaveInfo reserverInfo, int paidAmount) {
        String payAmountMessage = PAY_AMOUNT_MESSAGE_FORMAT.formatted(paidAmount);

        //TODO TO.채린 ~ SendMessageService 하나 생성해도 될 듯. => SendMessageToReservation / 나머지도 해당 서비스에서 다 전송하도록 수정
        try {
            // 새로운 메시지 템플릿 시스템 사용
            MessageTemplate messageTemplate = generateMessageService.getMessageForReservationConfirmation(
                    reserverInfo.getName(),
                    reservation.getExpo().getTitle(),
                    reservation.getReservationCode(),
                    reservation.getQuantity(),
                    payAmountMessage,
                    reservation.getUserType()
            );

            emailSendService.sendMail(
                    reserverInfo.getEmail(),
                    messageTemplate.getSubject(),
                    messageTemplate.getContent()
            );

            log.info("예매 완료 이메일 전송 완료 - 예약 ID: {}, 사용자 유형: {}, 이메일: {}",
                    reservation.getId(), reservation.getUserType(), reserverInfo.getEmail());
        } catch (Exception e) {
            log.error("예매 완료 이메일 전송 실패 - 예약 ID: {}, 오류: {}", reservation.getId(), e.getMessage());
        }
    }
}
