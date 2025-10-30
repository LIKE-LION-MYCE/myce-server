package com.myce.payment.service.mapper;

import com.myce.payment.dto.PaymentInfoDetailDto;
import com.myce.payment.dto.PaymentVerifyInfo;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.service.constant.PortOneResponseKey;
import com.myce.reservation.entity.Reservation;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    // PaymentVerifyRequest와 PortOne 응답을 기반으로 Payment 엔티티 생성
    public Payment toEntity(PaymentVerifyInfo request, Map<String, Object> portOnePayment) {
        String payMethod = (String) portOnePayment.get(PortOneResponseKey.PAY_METHOD);
        return Payment.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .paymentMethod(PaymentMethod.getPaymentMethod(payMethod))
                .provider((String) portOnePayment.get(PortOneResponseKey.PG_PROVIDER))
                .merchantUid(request.getMerchantUid())
                .impUid(request.getImpUid())
                .cardCompany((String) portOnePayment.get(PortOneResponseKey.CARD_NAME))
                .cardNumber((String) portOnePayment.get(PortOneResponseKey.CARD_NUMBER))
                .accountBank((String) portOnePayment.get(PortOneResponseKey.VBANK_NAME))
                .accountNumber((String) portOnePayment.get(PortOneResponseKey.VBANK_NUM))
                .country((String) portOnePayment.get(PortOneResponseKey.COUNTY))
                .paidAt(toPaidAtLocalDateTime(PortOneResponseKey.PAID_AT))
                .build();
    }

    // Payment 엔티티와 PaymentInfo 엔티티를 기반으로 PaymentVerifyResponse 생성
    public PaymentVerifyResponse toPaymentVerifyResponse(
            Payment payment, PaymentInfoDetailDto paymentInfoDetail) {
        return PaymentVerifyResponse.builder()
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .status(paymentInfoDetail.getStatus())
                .amount(paymentInfoDetail.getAmount())
                .reservationId(paymentInfoDetail.getReservationId())
                .build();
    }

    public ReservationPaymentInfo toReservationPaymentInfo(Reservation reservation, Integer paidAmount,
                                    PaymentStatus paymentStatus, int usedMileage, int savedMileage) {
        return ReservationPaymentInfo.builder()
            .reservation(reservation)
            .totalAmount(paidAmount)
            .status(paymentStatus)
            .usedMileage(usedMileage)
            .savedMileage(savedMileage)
            .build();
    }

    private LocalDateTime toPaidAtLocalDateTime(Object paidAtObj) {
        if (paidAtObj instanceof Integer) {
            return toLocalDateTime(((Integer) paidAtObj).longValue());
        } else if (paidAtObj instanceof Long) {
            return toLocalDateTime((Long) paidAtObj);
        }
        return null;
    }

    // Unix 타임스탬프를 LocalDateTime으로 변환
    private LocalDateTime toLocalDateTime(Long unixTimestamp) {
        if (unixTimestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault());
    }

    // PaymentVerifyRequest와 PortOne 응답을 기반으로 Payment 엔티티 생성
    public Payment toEntityTransfer(PaymentVerifyInfo request, Map<String, Object> portOnePayment) {
        String payMethod = (String) portOnePayment.get(PortOneResponseKey.PAY_METHOD);
        return Payment.builder()
            .targetType(request.getTargetType())
            .targetId(request.getTargetId())
            .paymentMethod(PaymentMethod.getPaymentMethod(payMethod))
            .provider((String) portOnePayment.get(PortOneResponseKey.PG_PROVIDER))
            .merchantUid(request.getMerchantUid())
            .impUid(request.getImpUid())
            .cardCompany((String) portOnePayment.get(PortOneResponseKey.CARD_NAME))
            .cardNumber((String) portOnePayment.get(PortOneResponseKey.CARD_NUMBER))
            .accountBank((String) portOnePayment.get(PortOneResponseKey.BANK_NAME))
            .accountNumber((String) portOnePayment.get(PortOneResponseKey.BANK_CODE))
            .country((String) portOnePayment.get(PortOneResponseKey.COUNTY))
            .paidAt(toPaidAtLocalDateTime(portOnePayment.get(PortOneResponseKey.PAID_AT)))
            .build();
    }
}
