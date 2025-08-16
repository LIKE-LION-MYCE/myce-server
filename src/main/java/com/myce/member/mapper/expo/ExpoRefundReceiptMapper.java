package com.myce.member.mapper.expo;

import com.myce.common.entity.BusinessProfile;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Refund;
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
        
        // 디버깅 로그 추가
        System.out.println("=== 환불 계산 디버깅 ===");
        System.out.println("게시 시작일: " + displayStartDate);
        System.out.println("오늘: " + today);
        System.out.println("총 게시 일수: " + expoPaymentInfo.getTotalDay());
        System.out.println("계산된 사용 일수: " + usedDays);
        
        // 남은 일수 계산
        int remainingDays = expoPaymentInfo.getTotalDay() - usedDays;
        if (remainingDays < 0) remainingDays = 0;
        
        System.out.println("계산된 남은 일수: " + remainingDays);
        System.out.println("=====================");
        
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
    
    public ExpoRefundReceiptResponse toRefundHistoryDto(Expo expo,
                                                        BusinessProfile businessProfile,
                                                        ExpoPaymentInfo expoPaymentInfo,
                                                        Refund refund) {
        
        // 실제 환불 내역 기반으로 생성
        LocalDate refundDate = refund.getRefundedAt() != null ? 
            refund.getRefundedAt().toLocalDate() : refund.getCreatedAt().toLocalDate();
        
        // 기본 정보는 결제 정보에서 가져온다
        int depositAmount = expo.getIsPremium() ? 
            expoPaymentInfo.getPremiumDeposit() : 
            expoPaymentInfo.getDeposit();
        
        int totalUsageFee = expoPaymentInfo.getTotalDay() * expoPaymentInfo.getDailyUsageFee();
        
        // 환불 종류에 따른 사용 일수 및 금액 계산
        int usedDays = 0;
        int usedAmount = 0;
        int remainingDays = expoPaymentInfo.getTotalDay();
        
        if (refund.getIsPartial()) {
            // 부분 환불인 경우: 환불 금액으로 남은 일수 계산 후 사용 일수 도출
            if (expoPaymentInfo.getDailyUsageFee() > 0) {
                remainingDays = refund.getAmount() / expoPaymentInfo.getDailyUsageFee();
                usedDays = expoPaymentInfo.getTotalDay() - remainingDays;
                usedAmount = usedDays * expoPaymentInfo.getDailyUsageFee();
                
                // 디버깅 로그 추가 (환불 완료 내역)
                System.out.println("=== 환불 완료 내역 디버깅 (수정된 로직) ===");
                System.out.println("총 게시 일수: " + expoPaymentInfo.getTotalDay());
                System.out.println("환불 금액: " + refund.getAmount());
                System.out.println("일일 이용료: " + expoPaymentInfo.getDailyUsageFee());
                System.out.println("계산된 남은 일수: " + remainingDays);
                System.out.println("계산된 사용 일수: " + usedDays);
                System.out.println("계산된 사용 금액: " + usedAmount);
                System.out.println("=====================");
            }
        }
        // 전액 환불인 경우는 기본값(0) 유지
        
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
                .refundRequestDate(refundDate) // 실제 환불 요청일
                .usedDays(usedDays)
                .usedAmount(usedAmount)
                .remainingDays(remainingDays)
                .refundAmount(refund.getAmount()) // 실제 환불된 금액
                .build();
    }
}