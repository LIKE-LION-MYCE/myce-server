package com.myce.member.mapper.expo;

import com.myce.common.entity.BusinessProfile;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.payment.entity.ExpoPaymentInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ExpoRefundReceiptMapper {
    
    public ExpoRefundReceiptResponse toRefundReceiptDto(Expo expo, 
                                                       BusinessProfile businessProfile, 
                                                       ExpoPaymentInfo expoPaymentInfo) {
        
        // 환불 계산
        LocalDate today = LocalDate.now();
        LocalDate displayStartDate = expo.getDisplayStartDate();
        
        // 사용한 일수 계산 (게시 시작일부터 오늘까지)
        int usedDays = (int) ChronoUnit.DAYS.between(displayStartDate, today) + 1; // +1은 시작일 포함
        if (usedDays < 0) usedDays = 0;
        
        // 남은 일수 계산
        int remainingDays = expoPaymentInfo.getTotalDay() - usedDays;
        if (remainingDays < 0) remainingDays = 0;
        
        // 등록금 계산 (프리미엄 여부에 따라)
        int depositAmount = expo.getIsPremium() ? 
            expoPaymentInfo.getPremiumDeposit() : 
            expoPaymentInfo.getDeposit();
        
        // 총 이용료 계산
        int totalUsageFee = expoPaymentInfo.getTotalDay() * expoPaymentInfo.getDailyUsageFee();
        
        // 금액 계산 - 엑스포 상태에 따라 환불 금액 결정
        int usedAmount;
        int refundAmount;
        
        if (expo.getStatus() == ExpoStatus.PENDING_PUBLISH) {
            // 게시 대기 상태: 전액 환불 (등록금 + 전체 이용료)
            usedAmount = 0;
            refundAmount = depositAmount + totalUsageFee;
        } else {
            // 게시 중 또는 기타 상태: 부분 환불 (남은 이용료만)
            usedAmount = usedDays * expoPaymentInfo.getDailyUsageFee();
            refundAmount = remainingDays * expoPaymentInfo.getDailyUsageFee();
        }
        
        return ExpoRefundReceiptResponse.builder()
                .expoTitle(expo.getTitle())
                .applicantName(businessProfile.getCompanyName())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .status(expo.getStatus())
                .totalDays(expoPaymentInfo.getTotalDay())
                .dailyUsageFee(expoPaymentInfo.getDailyUsageFee())
                .depositAmount(depositAmount)
                .totalUsageFee(totalUsageFee)
                .totalAmount(expoPaymentInfo.getTotalAmount())
                .isPremium(expo.getIsPremium())
                .refundRequestDate(today)
                .usedDays(usedDays)
                .usedAmount(usedAmount)
                .remainingDays(remainingDays)
                .refundAmount(refundAmount)
                .build();
    }
}