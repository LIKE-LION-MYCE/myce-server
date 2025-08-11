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
public class AdvertisementPaymentDetailResponse {
    
    // 광고 기본 정보
    private String advertisementTitle;
    private String applicantName;  // 신청자 (회사명)
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    
    // 결제 정보
    private Integer totalDays;         // 총 일수
    private Integer feePerDay;         // 일당 요금
    private Integer totalAmount;       // 총 결제해야 할 금액
    
    // 광고 상태
    private AdvertisementStatus status;  // 광고 현재 상태
}