package com.myce.expo.service.mapper;

import com.myce.common.entity.BusinessProfile;
import com.myce.expo.dto.ExpoPaymentPreviewResponse;
import com.myce.expo.entity.Expo;
import com.myce.system.entity.ExpoFeeSetting;

import java.time.format.DateTimeFormatter;

/**
 * 박람회 결제 정보 미리보기 DTO 변환 Mapper
 * 팀 코드 스타일: 정적 메서드로 변환 로직 구현
 */
public class ExpoPaymentPreviewMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Expo 엔티티와 계산된 정보를 ExpoPaymentPreviewResponse DTO로 변환
     * 
     * @param expo 박람회 엔티티
     * @param businessProfile 사업자 정보
     * @param feeSetting 수수료 설정
     * @param totalDays 총 일수
     * @param totalAmount 총 금액
     * @return 결제 정보 미리보기 응답 DTO
     */
    public static ExpoPaymentPreviewResponse toDto(
            Expo expo,
            BusinessProfile businessProfile,
            ExpoFeeSetting feeSetting,
            int totalDays,
            Integer totalAmount
    ) {
        // 사용료 계산
        Integer dailyUsageFee = feeSetting.getDailyUsageFee() != null ? feeSetting.getDailyUsageFee() : 0;
        Integer usageFeeAmount = dailyUsageFee * totalDays;
        
        // 보증금 설정
        Integer depositAmount = feeSetting.getDeposit() != null ? feeSetting.getDeposit() : 0;
        Integer premiumDepositAmount = expo.getIsPremium() && feeSetting.getPremiumDeposit() != null 
                ? feeSetting.getPremiumDeposit() 
                : null;
        
        // 신청자명 결정 (사업자 정보 우선, 없으면 회원 이름)
        String applicantName = businessProfile != null && businessProfile.getCeoName() != null 
                ? businessProfile.getCeoName()
                : expo.getMember().getName();
        
        return ExpoPaymentPreviewResponse.builder()
                .expoTitle(expo.getTitle())
                .applicantName(applicantName)
                .displayStartDate(expo.getDisplayStartDate().format(DATE_FORMATTER))
                .displayEndDate(expo.getDisplayEndDate().format(DATE_FORMATTER))
                .totalDays(totalDays)
                .dailyUsageFee(dailyUsageFee)
                .usageFeeAmount(usageFeeAmount)
                .depositAmount(depositAmount)
                .premiumDepositAmount(premiumDepositAmount)
                .totalAmount(totalAmount)
                .isPremium(expo.getIsPremium())
                .commissionRate(feeSetting.getSettlementCommission() != null 
                        ? feeSetting.getSettlementCommission().doubleValue() 
                        : 0.0)
                .build();
    }
}