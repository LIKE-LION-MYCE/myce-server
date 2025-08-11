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
public class AdvertisementRefundReceiptResponse {
    
    // 광고 기본 정보
    private String advertisementTitle;
    private String applicantName;  // 신청자 (회사명)
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private AdvertisementStatus status;
    
    // 원본 결제 정보
    private Integer totalDays;         // 총 일수
    private Integer feePerDay;         // 일당 요금
    private Integer totalAmount;       // 총 결제 금액
    
    // 사용 정보
    private LocalDate refundRequestDate;  // 환불 요청일 (오늘)
    private Integer usedDays;             // 사용한 일수
    private Integer usedAmount;           // 사용한 금액
    
    // 환불 정보
    private Integer remainingDays;        // 남은 일수
    private Integer refundAmount;         // 환불 금액 (남은일수 * 일이용료)
}