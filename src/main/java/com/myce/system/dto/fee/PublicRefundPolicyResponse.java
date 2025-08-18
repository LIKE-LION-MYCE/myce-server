package com.myce.system.dto.fee;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PublicRefundPolicyResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String standardType;
    private final int standardDayCount;
    private final BigDecimal feeRate;
    private final BigDecimal refundRate;
    private final String displayText;
    
    public static PublicRefundPolicyResponse from(RefundFeeResponse refundFee) {
        // 환불률 계산 (1 - 수수료율)
        BigDecimal refundRate = BigDecimal.ONE.subtract(refundFee.getFeeRate());
        
        // 표시 텍스트 생성
        String displayText = createDisplayText(refundFee.getName(), refundRate);
        
        return PublicRefundPolicyResponse.builder()
                .id(refundFee.getId())
                .name(refundFee.getName())
                .description(refundFee.getDescription())
                .standardType(refundFee.getStandardType())
                .standardDayCount(refundFee.getStandardDayCount())
                .feeRate(refundFee.getFeeRate())
                .refundRate(refundRate)
                .displayText(displayText)
                .build();
    }
    
    private static String createDisplayText(String name, BigDecimal refundRate) {
        int refundPercentage = refundRate.multiply(BigDecimal.valueOf(100)).intValue();
        
        if (refundPercentage == 0) {
            return name + ": 환불 불가";
        } else {
            return name + ": " + refundPercentage + "% 환불";
        }
    }
}