package com.myce.member.mapper.expo;

import com.myce.common.entity.BusinessProfile;
import com.myce.expo.entity.Expo;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.payment.entity.ExpoPaymentInfo;
import org.springframework.stereotype.Component;

@Component
public class ExpoPaymentDetailMapper {
    
    public ExpoPaymentDetailResponse toExpoPaymentDetailResponse(Expo expo, 
                                                                BusinessProfile businessProfile, 
                                                                ExpoPaymentInfo expoPaymentInfo) {
        
        // 사용료 총액 계산 (일당 * 총일수)
        int usageFeeAmount = expoPaymentInfo.getDailyUsageFee() * expoPaymentInfo.getTotalDay();
        
        // 등록금 계산 (프리미엄이면 premiumDeposit, 아니면 deposit)
        int depositAmount = expo.getIsPremium() ? 
                expoPaymentInfo.getPremiumDeposit() : 
                expoPaymentInfo.getDeposit();
        
        return ExpoPaymentDetailResponse.builder()
                .expoTitle(expo.getTitle())
                .applicantName(businessProfile != null ? businessProfile.getCompanyName() : "")
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .status(expo.getStatus())
                .totalDays(expoPaymentInfo.getTotalDay())
                .dailyUsageFee(expoPaymentInfo.getDailyUsageFee())
                .usageFeeAmount(usageFeeAmount)
                .depositAmount(depositAmount)
                .totalAmount(expoPaymentInfo.getTotalAmount())
                .isPremium(expo.getIsPremium())
                .build();
    }
}