package com.myce.member.mapper.expo;

import com.myce.common.entity.BusinessProfile;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Refund;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
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
            expoPaymentInfo.getPremiumDeposit() + expoPaymentInfo.getDeposit() :
            expoPaymentInfo.getDeposit();
        
        // 총 이용료 계산
        int totalUsageFee = expoPaymentInfo.getTotalDay() * expoPaymentInfo.getDailyUsageFee();
        
        // 금액 계산 - 엑스포 상태에 따라 환불 금액 결정
        int usedAmount;
        int refundAmount;
        
        if (expo.getStatus() == ExpoStatus.PENDING_PUBLISH || expo.getStatus() == ExpoStatus.PENDING_CANCEL) {
            // 게시 대기 상태 또는 취소 대기 상태: 전액 환불 (등록금 + 전체 이용료)
            // PENDING_CANCEL 상태는 PENDING_PUBLISH에서 환불 신청한 경우이므로 전액 환불
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
                .refundReason(null) // 아직 환불 신청 전이므로 null
                .build();
    }
    
    /**
     * Refund 테이블 데이터를 기반으로 환불 신청서 생성
     * 팀원 제안: is_partial 필드를 보고 환불 금액 처리
     */
    public ExpoRefundReceiptResponse toRefundReceiptWithRefundData(Expo expo,
                                                                   BusinessProfile businessProfile,
                                                                   ExpoPaymentInfo expoPaymentInfo,
                                                                   Refund refund) {
        
        log.info("toRefundReceiptWithRefundData 호출 - refund.amount: {}, refund.isPartial: {}", 
                refund.getAmount(), refund.getIsPartial());
        
        // 등록금 계산 (프리미엄 여부에 따라)
        int depositAmount = expo.getIsPremium() ? 
            expoPaymentInfo.getPremiumDeposit() : 
            expoPaymentInfo.getDeposit();
        
        // 총 이용료 계산
        int totalUsageFee = expoPaymentInfo.getTotalDay() * expoPaymentInfo.getDailyUsageFee();
        
        log.info("계산값 - depositAmount: {}, totalUsageFee: {}", depositAmount, totalUsageFee);
        
        // Refund 테이블의 is_partial 필드를 기반으로 계산
        int usedDays;
        int usedAmount;
        int remainingDays;
        
        if (refund.getIsPartial()) {
            log.info("부분 환불 로직 실행");
            // 부분 환불: 사용한 일수 계산
            LocalDate today = LocalDate.now();
            LocalDate displayStartDate = expo.getDisplayStartDate();
            usedDays = (int) ChronoUnit.DAYS.between(displayStartDate, today) + 1;
            if (usedDays < 0) usedDays = 0;
            
            remainingDays = expoPaymentInfo.getTotalDay() - usedDays;
            if (remainingDays < 0) remainingDays = 0;
            
            usedAmount = usedDays * expoPaymentInfo.getDailyUsageFee();
        } else {
            log.info("전액 환불 로직 실행");
            // 전액 환불: 사용한 일수 0
            usedDays = 0;
            usedAmount = 0;
            remainingDays = expoPaymentInfo.getTotalDay();
        }
        
        log.info("최종 환불 금액: {}", refund.getAmount());
        
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
                .refundRequestDate(refund.getCreatedAt().toLocalDate())
                .usedDays(usedDays)
                .usedAmount(usedAmount)
                .remainingDays(remainingDays)
                .refundAmount(refund.getAmount()) // Refund 테이블의 실제 환불 금액 사용
                .refundReason(refund.getReason()) // 환불 사유 추가
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
                .refundReason(refund.getReason()) // 환불 사유
                .build();
    }
}