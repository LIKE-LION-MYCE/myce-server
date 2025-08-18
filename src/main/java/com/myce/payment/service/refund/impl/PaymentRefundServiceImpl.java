package com.myce.payment.service.refund.impl;

import com.myce.advertisement.repository.AdRepository;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.notification.service.NotificationService;
import com.myce.payment.dto.PaymentImpUidForRefundRequest;
import com.myce.payment.dto.PaymentInfoForRefund;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.AdRefundRequest;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.refund.PaymentRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundServiceImpl implements PaymentRefundService {

    private final PortOneApiService portOneApiService;
    private final PaymentRepository paymentRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final RefundRepository refundRepository;
    private final AdRepository advertisementRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Map<String, Object> refundPayment(PaymentRefundRequest request) {
        log.info("[포트원 환불 impUid] impUid={}", request.getImpUid());
        // 1. 포트원 액세스 토큰 발급
        String accessToken = portOneApiService.getAccessToken();

        // 2. 결제 엔티티 조회
        Payment payment = paymentRepository.findByImpUid(request.getImpUid())
                .orElseGet(() -> paymentRepository.findByMerchantUid(request.getMerchantUid())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND)));

        // 3. 환불 대상 결제 상세 정보 및 원 결제 금액 조회
        PaymentInfoForRefund paymentInfoForRefund = getPaymentInfoForRefund(payment);
        Object paymentInfoEntity = paymentInfoForRefund.getPaymentInfoEntity();
        Integer originalPaidAmount = paymentInfoForRefund.getOriginalPaidAmount();

        // 4. 포트원 환불 요청
        Map<String, Object> responseBody = portOneApiService.requestRefundToPortOne(request, originalPaidAmount, accessToken);

        // 5. 환불 처리 로컬 데이터 저장/상태 변경
        processRefund(request, payment, paymentInfoEntity, originalPaidAmount, responseBody);

        // 6. 포트원 응답 반환
        return responseBody;
    }

    // 환불 내역 저장 및 결제 상태 업데이트
    private void processRefund(PaymentRefundRequest request, Payment payment, Object paymentInfoEntity,
                               Integer originalPaidAmount, Map<String, Object> responseBody) {
        // 환불 금액
        Integer refundedAmount = (Integer) responseBody.get("cancel_amount");
        
        // 부분 환불 여부
        boolean isPartial =
                request.getCancelAmount() != null && request.getCancelAmount() < originalPaidAmount;

        // 환불 엔티티 처리 (타겟 타입에 따라 다르게 처리)
        if (payment.getTargetType() == PaymentTargetType.RESERVATION) {
            // RESERVATION: 새로 생성
            Refund refund = Refund.builder()
                    .payment(payment)
                    .amount(refundedAmount)
                    .reason(request.getReason())
                    .status(RefundStatus.REFUNDED)
                    .isPartial(isPartial)
                    .refundedAt(LocalDateTime.now())
                    .build();
            refundRepository.save(refund);
        } else {
            // AD, EXPO: 기존 PENDING 상태를 REFUNDED로 업데이트
            Optional<Refund> existingRefund = refundRepository.findByPaymentAndStatus(payment, RefundStatus.PENDING);
            if (existingRefund.isPresent()) {
                Refund refund = existingRefund.get();
                refund.updateToRefund(); // PENDING -> REFUNDED로 상태 변경
                refundRepository.save(refund);
            } else {
                // PENDING 상태의 환불이 없는 경우 새로 생성 (fallback)
                Refund refund = Refund.builder()
                        .payment(payment)
                        .amount(refundedAmount)
                        .reason(request.getReason())
                        .status(RefundStatus.REFUNDED)
                        .isPartial(isPartial)
                        .refundedAt(LocalDateTime.now())
                        .build();
                refundRepository.save(refund);
            }
        }

        updatePaymentInfoStatus(paymentInfoEntity, isPartial);
    }

    // 결제 상세 상태 업데이트
    private void updatePaymentInfoStatus(Object paymentInfoEntity, boolean isPartial) {
        if (paymentInfoEntity instanceof ReservationPaymentInfo) {
            ReservationPaymentInfo rpi = (ReservationPaymentInfo) paymentInfoEntity;
            rpi.setStatus(isPartial ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED);
            reservationPaymentInfoRepository.save(rpi);
        } else if (paymentInfoEntity instanceof AdPaymentInfo) {
            AdPaymentInfo api = (AdPaymentInfo) paymentInfoEntity;
            api.setStatus(isPartial ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED);
            adPaymentInfoRepository.save(api);
        } else if (paymentInfoEntity instanceof ExpoPaymentInfo) {
            ExpoPaymentInfo epi = (ExpoPaymentInfo) paymentInfoEntity;
            epi.setStatus(isPartial ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED);
            expoPaymentInfoRepository.save(epi);
        }
    }

    // 결제 대상(Reservation, ad, expo)에 따른 결제 상세 및 금액 조회
    private PaymentInfoForRefund getPaymentInfoForRefund(Payment payment) {
        Object paymentInfoEntity;
        Integer originalPaidAmount;

        switch (payment.getTargetType()) {
            case RESERVATION:
                ReservationPaymentInfo rpi = reservationPaymentInfoRepository.findByReservationId(
                                payment.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
                paymentInfoEntity = rpi;
                originalPaidAmount = rpi.getTotalAmount();
                break;
            case AD:
                AdPaymentInfo api = adPaymentInfoRepository.findByAdvertisementId(payment.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
                paymentInfoEntity = api;
                originalPaidAmount = api.getTotalAmount();
                break;
            case EXPO:
                ExpoPaymentInfo epi = expoPaymentInfoRepository.findByExpoId(payment.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
                paymentInfoEntity = epi;
                originalPaidAmount = epi.getTotalAmount();
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
        }
        return new PaymentInfoForRefund(paymentInfoEntity, originalPaidAmount);
    }

    @Override
    public String getImpUidForRefund(PaymentImpUidForRefundRequest request) {
        Payment payment = paymentRepository.findByTargetIdAndTargetType(request.getTargetId(), request.getTargetType())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        return payment.getImpUid();
    }

    @Override
    @Transactional
    public Map<String, Object> processAdRefund(AdRefundRequest request) {
        log.info("[광고 통합 환불 처리] adId={}, cancelAmount={}", request.getAdId(), request.getCancelAmount());
        
        // 1. 광고 정보 조회
        Advertisement advertisement = advertisementRepository.findById(request.getAdId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        
        // 2. 결제 정보 조회
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(request.getAdId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        Payment payment = paymentRepository.findByTargetIdAndTargetType(request.getAdId(), PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        // 3. 포트원 환불 요청을 위한 기존 로직 재사용
        PaymentRefundRequest refundRequest = new PaymentRefundRequest();
        refundRequest.setImpUid(payment.getImpUid());
        refundRequest.setCancelAmount(request.getCancelAmount());
        refundRequest.setReason(request.getReason());
        
        // 4. 포트원 환불 처리
        Map<String, Object> portOneResponse = refundPayment(refundRequest);
        
        // 5. 광고 상태를 CANCELLED로 변경
        String oldStatus = advertisement.getStatus().name();
        advertisement.updateStatus(AdvertisementStatus.CANCELLED);
        String newStatus = advertisement.getStatus().name();
        advertisementRepository.save(advertisement);

        try {
            notificationService.sendAdvertisementStatusChangeNotification(
                    request.getAdId(), advertisement.getTitle(), oldStatus, newStatus);
        } catch (Exception e) {
            log.warn("광고 환불 승인 알림 전송 실패 - adId: {}, 오류: {}", request.getAdId(), e.getMessage());
        }


        // 6. 결제 상태 업데이트 (부분환불/전체환불 구분)
        // 프론트에서 cancelAmount가 있으면 부분환불, null이면 전체환불
        boolean isPartialRefund = request.getCancelAmount() != null;
        adPaymentInfo.setStatus(isPartialRefund ? PaymentStatus.PARTIAL_REFUNDED : PaymentStatus.REFUNDED);
        adPaymentInfoRepository.save(adPaymentInfo);
        
        log.info("[광고 통합 환불 완료] adId={}, isPartial={}", request.getAdId(), isPartialRefund);
        return portOneResponse;
    }
}
