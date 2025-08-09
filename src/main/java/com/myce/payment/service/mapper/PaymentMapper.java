package com.myce.payment.service.mapper;

import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentMethod;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    // PaymentVerifyRequest와 PortOne 응답을 기반으로 Payment 엔티티 생성
    public static Payment toEntity(PaymentVerifyRequest request, Map<String, Object> portOnePayment) {
        return Payment.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .paymentMethod(toPaymentMethod(portOnePayment))
                .provider((String) portOnePayment.get("pg_provider"))
                .merchantUid(request.getMerchantUid())
                .impUid(request.getImpUid())
                .cardCompany((String) portOnePayment.get("card_name"))
                .cardNumber((String) portOnePayment.get("card_number"))
                .accountBank((String) portOnePayment.get("bank_name"))
                .accountNumber((String) portOnePayment.get("account_number"))
                .country((String) portOnePayment.get("country"))
                .paidAt(toPaidAtLocalDateTime(portOnePayment.get("paid_at")))
                .build();
    }

    // Payment 엔티티와 PaymentInfo 엔티티를 기반으로 PaymentVerifyResponse 생성
    public static PaymentVerifyResponse toPaymentVerifyResponse(Payment payment, Object paymentInfo) {
        String status = null;
        Integer amount = null;

        if (paymentInfo instanceof ReservationPaymentInfo) {
            ReservationPaymentInfo info = (ReservationPaymentInfo) paymentInfo;
            status = info.getStatus().name();
            amount = info.getTotalAmount();
        } else if (paymentInfo instanceof AdPaymentInfo) {
            AdPaymentInfo info = (AdPaymentInfo) paymentInfo;
            status = info.getStatus().name();
            amount = info.getTotalAmount();
        } else if (paymentInfo instanceof ExpoPaymentInfo) {
            ExpoPaymentInfo info = (ExpoPaymentInfo) paymentInfo;
            status = info.getStatus().name();
            amount = info.getTotalAmount();
        }

        return PaymentVerifyResponse.builder()
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .status(status)
                .amount(amount)
                .build();
    }

    // PortOne의 pay_method 값을 PaymentMethod enum으로 변환
    private static PaymentMethod toPaymentMethod(Map<String, Object> portOnePayment) {
        String payMethod = (String) portOnePayment.get("pay_method");
        if (payMethod == null) return null;
        return switch (payMethod) {
            case "card" -> PaymentMethod.CARD;
            case "trans" -> PaymentMethod.TRANSFER;
            case "vbank" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "samsung", "kakaopay", "naverpay", "payco", "lpay", "ssgpay", "tosspay"
                -> PaymentMethod.EASY_PAY;
            default -> throw new IllegalArgumentException("Unknown payment method: " + payMethod);
        };
    }

    private static LocalDateTime toPaidAtLocalDateTime(Object paidAtObj) {
        if (paidAtObj instanceof Integer) {
            return toLocalDateTime(((Integer) paidAtObj).longValue());
        } else if (paidAtObj instanceof Long) {
            return toLocalDateTime((Long) paidAtObj);
        }
        return null;
    }

    // Unix 타임스탬프를 LocalDateTime으로 변환
    private static LocalDateTime toLocalDateTime(Long unixTimestamp) {
        if (unixTimestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault());
    }
}
