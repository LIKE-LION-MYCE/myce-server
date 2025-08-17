package com.myce.member.dto.expo;

import com.myce.expo.entity.type.ExpoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpoRefundReceiptResponse {
    
    // 박람회 기본 정보
    private String expoTitle;
    private String applicantName;  // 신청자 (회사명)
    private LocalDate displayStartDate;  // 게시 시작일
    private LocalDate displayEndDate;    // 게시 종료일
    private ExpoStatus status;
    
    // 원본 결제 정보
    private Integer totalDays;         // 총 게시 일수
    private Integer dailyUsageFee;     // 일일 이용료
    private Integer depositAmount;     // 등록금 (프리미엄/기본)
    private Integer totalUsageFee;     // 총 이용료 (일수 * 일일이용료)
    private Integer totalAmount;       // 총 결제 금액 (등록금 + 총이용료)
    private Boolean isPremium;         // 프리미엄 여부
    
    // 사용 정보
    private LocalDate refundRequestDate;  // 환불 요청일 (오늘)
    private Integer usedDays;             // 사용한 일수
    private Integer usedAmount;           // 사용한 금액
    
    // 환불 정보
    private Integer remainingDays;        // 남은 게시 일수
    private Integer refundAmount;         // 환불 금액 (남은일수 * 일일이용료)
    private String refundReason;          // 환불 사유
}