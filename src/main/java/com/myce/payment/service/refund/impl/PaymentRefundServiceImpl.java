package com.myce.payment.service.refund.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.dto.PaymentImpUidForRefundRequest;
import com.myce.payment.dto.PaymentInfoForRefund;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.refund.PaymentRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

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

        // 환불 엔티티 저장
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(refundedAmount)
                .reason(request.getReason())
                .status(RefundStatus.REFUNDED)
                .isPartial(isPartial)
                .refundedAt(LocalDateTime.now())
                .build();
        refundRepository.save(refund);

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
}
