package com.myce.member.dto.ad;

import com.myce.advertisement.entity.type.AdvertisementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdRefundReceiptResponse {
    
    // 광고 기본 정보
    private String adTitle;
    private String applicantName;  // 신청자 (회사명)
    private LocalDate displayStartDate;  // 게시 시작일
    private LocalDate displayEndDate;    // 게시 종료일
    private AdvertisementStatus status;
    
    // 원본 결제 정보
    private Integer totalDays;         // 총 게시 일수
    private Integer dailyFee;          // 일일 요금
    private Integer totalAmount;       // 총 결제 금액
    
    // 사용 정보
    private LocalDate refundRequestDate;  // 환불 요청일
    private Integer usedDays;             // 사용한 일수
    private Integer usedAmount;           // 사용한 금액
    
    // 환불 정보
    private Integer remainingDays;        // 남은 게시 일수
    private Integer refundAmount;         // 환불 금액
}