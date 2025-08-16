package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 박람회 승인 시 결제 정보 미리보기 응답 DTO
 * expo_payment_info 테이블 생성 전 계산된 정보를 제공
 */
@Getter
@NoArgsConstructor
public class ExpoPaymentPreviewResponse {
    
    private String expoTitle;
    private String applicantName;
    private String displayStartDate;
    private String displayEndDate;
    private Integer totalDays;
    private Integer dailyUsageFee;
    private Integer usageFeeAmount;
    private Integer depositAmount;
    private Integer premiumDepositAmount;
    private Integer totalAmount;
    private Boolean isPremium;
    private Double commissionRate;
    
    @Builder
    public ExpoPaymentPreviewResponse(
            String expoTitle,
            String applicantName,
            String displayStartDate,
            String displayEndDate,
            Integer totalDays,
            Integer dailyUsageFee,
            Integer usageFeeAmount,
            Integer depositAmount,
            Integer premiumDepositAmount,
            Integer totalAmount,
            Boolean isPremium,
            Double commissionRate
    ) {
        this.expoTitle = expoTitle;
        this.applicantName = applicantName;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.totalDays = totalDays;
        this.dailyUsageFee = dailyUsageFee;
        this.usageFeeAmount = usageFeeAmount;
        this.depositAmount = depositAmount;
        this.premiumDepositAmount = premiumDepositAmount;
        this.totalAmount = totalAmount;
        this.isPremium = isPremium;
        this.commissionRate = commissionRate;
    }
}