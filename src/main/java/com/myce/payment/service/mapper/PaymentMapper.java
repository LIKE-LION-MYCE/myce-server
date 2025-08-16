package com.myce.payment.service.mapper;

import com.myce.advertisement.entity.Advertisement;
import com.myce.expo.entity.Expo;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.reservation.entity.Reservation;
import com.myce.system.entity.AdFeeSetting;
import com.myce.system.entity.ExpoFeeSetting;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    // PaymentVerifyRequest와 PortOne 응답을 기반으로 Payment 엔티티 생성
    public Payment toEntity(PaymentVerifyRequest request, Map<String, Object> portOnePayment) {
        return Payment.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .paymentMethod(toPaymentMethod(portOnePayment))
                .provider((String) portOnePayment.get("pg_provider"))
                .merchantUid(request.getMerchantUid())
                .impUid(request.getImpUid())
                .cardCompany((String) portOnePayment.get("card_name"))
                .cardNumber((String) portOnePayment.get("card_number"))
                .accountBank((String) portOnePayment.get("vbank_name"))
                .accountNumber((String) portOnePayment.get("vbank_num"))
                .country((String) portOnePayment.get("country"))
                .paidAt(toPaidAtLocalDateTime(portOnePayment.get("paid_at")))
                .build();
    }

    // Payment 엔티티와 PaymentInfo 엔티티를 기반으로 PaymentVerifyResponse 생성
    public PaymentVerifyResponse toPaymentVerifyResponse(Payment payment, Object paymentInfo) {
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

    public ReservationPaymentInfo toReservationPaymentInfo(PaymentVerifyRequest request,
        Reservation reservation, Integer paidAmount, PaymentStatus paymentStatus) {
        return ReservationPaymentInfo.builder()
            .reservation(reservation)
            .totalAmount(paidAmount)
            .status(paymentStatus)
            .usedMileage(request.getUsedMileage())
            .savedMileage(request.getSavedMileage())
            .build();
    }

    public AdPaymentInfo toAdPaymentInfo(Advertisement advertisement, AdFeeSetting adFeeSetting,
        Integer paidAmount, PaymentStatus paymentStatus) {
        return AdPaymentInfo.builder()
            .advertisement(advertisement)
            .totalAmount(paidAmount)
            .status(paymentStatus)
            .totalDay(advertisement.getTotalDays())
            .feePerDay(adFeeSetting.getFeePerDay())
            .build();
    }

    public ExpoPaymentInfo toExpoPaymentInfo(Expo expo, ExpoFeeSetting expoFeeSetting,
        Integer paidAmount, PaymentStatus paymentStatus) {
        long totalDays = ChronoUnit.DAYS.between(expo.getDisplayStartDate(), expo.getDisplayEndDate()) + 1;
        return ExpoPaymentInfo.builder()
            .expo(expo)
            .totalAmount(paidAmount)
            .status(paymentStatus)
            .deposit(expoFeeSetting.getDeposit())
            .premiumDeposit(expoFeeSetting.getPremiumDeposit())
            .totalDay((int) totalDays)
            .dailyUsageFee(expoFeeSetting.getDailyUsageFee())
            .commissionRate(expoFeeSetting.getSettlementCommission())
            .build();
    }
    
    // PortOne의 pay_method 값을 PaymentMethod enum으로 변환
    private PaymentMethod toPaymentMethod(Map<String, Object> portOnePayment) {
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
    public Payment toEntityTransfer(PaymentVerifyRequest request, Map<String, Object> portOnePayment) {
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
            .accountNumber((String) portOnePayment.get("bank_code"))
            .country((String) portOnePayment.get("country"))
            .paidAt(toPaidAtLocalDateTime(portOnePayment.get("paid_at")))
            .build();
    }
}
