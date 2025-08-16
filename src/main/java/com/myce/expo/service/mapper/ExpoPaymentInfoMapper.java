package com.myce.expo.service.mapper;

import com.myce.expo.entity.Expo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.system.entity.ExpoFeeSetting;

import java.math.BigDecimal;

/**
 * ExpoPaymentInfo 매퍼 클래스
 * Entity 생성을 위한 정적 메서드 제공
 */
public class ExpoPaymentInfoMapper {

    /**
     * 박람회 승인 시 ExpoPaymentInfo 엔티티 생성
     *
     * @param expo 박람회 엔티티
     * @param feeSetting 수수료 설정
     * @param totalDays 총 일수
     * @param totalAmount 총 금액
     * @return ExpoPaymentInfo 엔티티
     */
    public static ExpoPaymentInfo toEntity(Expo expo, ExpoFeeSetting feeSetting, int totalDays, Integer totalAmount) {
        return ExpoPaymentInfo.builder()
                .expo(expo)
                .status(PaymentStatus.PENDING)
                .deposit(feeSetting.getDeposit() != null ? feeSetting.getDeposit() : 0)
                .premiumDeposit(feeSetting.getPremiumDeposit() != null ? feeSetting.getPremiumDeposit() : 0)
                .totalDay(totalDays)
                .dailyUsageFee(feeSetting.getDailyUsageFee() != null ? feeSetting.getDailyUsageFee() : 0)
                .totalAmount(totalAmount)
                .commissionRate(feeSetting.getSettlementCommission() != null ? 
                    feeSetting.getSettlementCommission() : BigDecimal.ZERO)
                .build();
    }
}