package com.myce.payment.service.mapper;

import com.myce.payment.dto.PaymentInfoResponse;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.type.PaymentTargetType;

import java.math.BigDecimal;

public class PaymentInfoMapper {
    public static PaymentInfoResponse expoPaymentInfoToResponse(ExpoPaymentInfo paymentInfo,
                                                                BigDecimal ticketBenefit) {
        return PaymentInfoResponse.builder()
                .id(paymentInfo.getId())
                .title(paymentInfo.getExpo().getTitle())
                .type(PaymentTargetType.EXPO.name())
                .serviceStartAt(paymentInfo.getExpo().getDisplayStartDate())
                .serviceEndAt(paymentInfo.getExpo().getDisplayEndDate())
                .createdAt(paymentInfo.getCreatedAt())
                .deposit(paymentInfo.getDeposit() + paymentInfo.getPremiumDeposit())
                .ticketBenefit(ticketBenefit)
                .totalBenefit(BigDecimal.valueOf(paymentInfo.getTotalAmount() + ticketBenefit.doubleValue()))
                .status(paymentInfo.getStatus().name())
                .build();
    }

    public static PaymentInfoResponse adPaymentInfoToResponse(AdPaymentInfo paymentInfo){

        return PaymentInfoResponse.builder()
                .id(paymentInfo.getId())
                .title(paymentInfo.getAdvertisement().getTitle())
                .type(PaymentTargetType.AD.name())
                .serviceStartAt(paymentInfo.getAdvertisement().getDisplayStartDate())
                .serviceEndAt(paymentInfo.getAdvertisement().getDisplayEndDate())
                .createdAt(paymentInfo.getCreatedAt())
                .deposit(paymentInfo.getTotalAmount())
                .totalBenefit(BigDecimal.valueOf(paymentInfo.getTotalAmount()))
                .status(paymentInfo.getStatus().name())
                .build();
    }
}
