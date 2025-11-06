package com.myce.payment.service.webhook.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.dto.PortOneWebhookRequest;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.constant.PortOneResponseKey;
import com.myce.payment.service.constant.PortOneStatus;
import com.myce.payment.service.impl.PaymentCommonService;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.webhook.PaymentWebhookService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PortOneApiService portOneApiService;
    private final PaymentRepository paymentRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final PaymentCommonService paymentCommonService;

    // 가상계좌 입금 처리 웹훅
    @Override
    @Transactional
    public void processWebhook(PortOneWebhookRequest request) {
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid());
        String status = (String) portOnePayment.get(PortOneResponseKey.STATUS);
        String portOneMerchantUid = (String) portOnePayment.get(PortOneResponseKey.MERCHANT_UID);
        int paidAmount = ((Number) portOnePayment.getOrDefault(PortOneResponseKey.AMOUNT, 0)).intValue();

        if (!PortOneStatus.PAID.equalsIgnoreCase(status)) {
            log.info("[웹훅 무시] 포트원 상태가 paid 아님. status={}", status);
            return;
        }

        // 결제 시점 받아옴
        long paidAt = ((Number) portOnePayment.getOrDefault(PortOneResponseKey.PAID_AT, 0)).longValue();
        Payment payment = paymentRepository.findByImpUid(request.getImpUid())
                .orElseGet(() -> paymentRepository.findByMerchantUid(request.getMerchantUid())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND)));

        long targetId = payment.getTargetId();
        switch (payment.getTargetType()) {
            case RESERVATION:
                processReservation(targetId, paidAmount);
                break;
            case AD:
                processAd(targetId, paidAmount);
                break;
            case EXPO:
                processExpo(targetId, paidAmount);
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
        }

        // TODO 타임존 확인하기
        payment.updateOnSuccess(LocalDateTime.ofEpochSecond(paidAt, 0, ZoneOffset.ofHours(9)));
        paymentRepository.save(payment);

        log.info("[웹훅 처리 완료] imp_uid={}, merchant_uid={}, paid_at={}",
                request.getImpUid(), portOneMerchantUid, paidAt);
    }

    private void processReservation(Long paymentTargetId, int paidAmount) {
        ReservationPaymentInfo reservationPaymentInfo = reservationPaymentInfoRepository
                .findByReservationId(paymentTargetId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        if (isPassStatus(reservationPaymentInfo.getStatus())) return;

        if (!reservationPaymentInfo.getTotalAmount().equals(paidAmount)) {
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        reservationPaymentInfo.setStatus(PaymentStatus.SUCCESS);

        // 가상계좌 입금 확인 시 reservation CONFIRMED로
        Reservation reservation = reservationPaymentInfo.getReservation();
        if(reservation == null) throw new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND);

        reservation.updateStatus(ReservationStatus.CONFIRMED);

        // QR 코드 생성 시도
        paymentCommonService.issueQrForReservation(reservation);

        UserType userType = reservation.getUserType();
        if (userType.equals(UserType.MEMBER)) {
            // 가상계좌 입금 완료 시 마일리지 및 등급 업데이트 (회원만)
            int usedMileage = Objects.requireNonNullElse(reservationPaymentInfo.getUsedMileage(), 0);
            int savedMileage = Objects.requireNonNullElse(reservationPaymentInfo.getSavedMileage(), 0);
            paymentCommonService.processMileage(usedMileage, savedMileage, reservation.getUserId());
            // 결제 완료 알림 발송 (회원만)
            paymentCommonService.sendAlert(reservation, paidAmount);
        }

        reservationPaymentInfoRepository.save(reservationPaymentInfo);
    }

    private void processAd(Long paymentTargetId, int paidAmount) {
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository
                .findByAdvertisementId(paymentTargetId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        if (isPassStatus(adPaymentInfo.getStatus())) return;

        if (!adPaymentInfo.getTotalAmount().equals(paidAmount)) {
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        adPaymentInfo.setStatus(PaymentStatus.SUCCESS);
        adPaymentInfoRepository.save(adPaymentInfo);
    }

    private void processExpo(long targetId, int paidAmount) {
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository
                .findByExpoId(targetId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        if (isPassStatus(expoPaymentInfo.getStatus())) return;

        if (!expoPaymentInfo.getTotalAmount().equals(paidAmount)) {
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        expoPaymentInfo.setStatus(PaymentStatus.SUCCESS);
        expoPaymentInfoRepository.save(expoPaymentInfo);
    }

    private boolean isPassStatus(PaymentStatus status) {
        return status.equals(PaymentStatus.SUCCESS)
                || status.equals(PaymentStatus.REFUNDED)
                || status.equals(PaymentStatus.PARTIAL_REFUNDED);
    }
}
