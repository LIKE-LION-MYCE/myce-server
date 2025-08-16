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
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.webhook.PaymentWebhookService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PortOneApiService portOneApiService;
    private final PaymentRepository paymentRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final ReservationRepository reservationRepository;

    // 가상계좌 입금 처리 웹훅
    @Override
    @Transactional
    public void processWebhook(PortOneWebhookRequest request) {
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> pay = portOneApiService.getPaymentInfo(request.getImpUid(), accessToken);

        String status = (String) pay.get("status");
        String portOneMerchantUid = (String) pay.get("merchant_uid");
        Integer paidAmount = ((Number) pay.getOrDefault("amount", 0)).intValue();

        if (!"paid".equalsIgnoreCase(status)) {
            log.info("[웹훅 무시] 포트원 상태가 paid 아님. status={}", status);
            return;
        }

        // 결제 시점 받아옴
        Long paidAt = ((Number) pay.getOrDefault("paid_at", 0)).longValue();

        Payment payment = paymentRepository.findByImpUid(request.getImpUid())
                .orElseGet(() -> paymentRepository.findByMerchantUid(request.getMerchantUid())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND)));

        // target type에 따라 분기해서 처리
        switch (payment.getTargetType()) {
            case RESERVATION:
                ReservationPaymentInfo rpi = reservationPaymentInfoRepository
                        .findByReservationId(payment.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

                if (rpi.getStatus() == PaymentStatus.SUCCESS
                        || rpi.getStatus() == PaymentStatus.REFUNDED
                        || rpi.getStatus() == PaymentStatus.PARTIAL_REFUNDED) {
                    return;
                }

                if (!Integer.valueOf(rpi.getTotalAmount()).equals(paidAmount)) {
                    throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
                }

                rpi.setStatus(PaymentStatus.SUCCESS);
                
                // 가상계좌 입금 확인 시 reservation CONFIRMED로
                Reservation reservation = reservationRepository.findById(payment.getTargetId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

                reservation.updateStatus(ReservationStatus.CONFIRMED);
                
                reservationPaymentInfoRepository.save(rpi);
                break;

            case AD:
                AdPaymentInfo api = adPaymentInfoRepository
                        .findByAdvertisementId(payment.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

                if (api.getStatus() == PaymentStatus.SUCCESS
                        || api.getStatus() == PaymentStatus.REFUNDED
                        || api.getStatus() == PaymentStatus.PARTIAL_REFUNDED) {
                    return;
                }

                if (!Integer.valueOf(api.getTotalAmount()).equals(paidAmount)) {
                    throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
                }

                api.setStatus(PaymentStatus.SUCCESS);
                adPaymentInfoRepository.save(api);
                break;

            case EXPO:
                ExpoPaymentInfo epi = expoPaymentInfoRepository
                        .findByExpoId(payment.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

                if (epi.getStatus() == PaymentStatus.SUCCESS
                        || epi.getStatus() == PaymentStatus.REFUNDED
                        || epi.getStatus() == PaymentStatus.PARTIAL_REFUNDED) {
                    return;
                }

                if (!Integer.valueOf(epi.getTotalAmount()).equals(paidAmount)) {
                    throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
                }

                epi.setStatus(PaymentStatus.SUCCESS);
                expoPaymentInfoRepository.save(epi);
                break;

            default:
                throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
        }
        payment.updateOnSuccess(
                LocalDateTime.ofEpochSecond(paidAt, 0, ZoneOffset.ofHours(9))
        );
        paymentRepository.save(payment);

        log.info("[웹훅 처리 완료] imp_uid={}, merchant_uid={}, paid_at={}",
                request.getImpUid(), portOneMerchantUid, paidAt);
    }
}
