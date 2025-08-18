package com.myce.system.service.mapper;

import com.myce.system.dto.fee.RefundFeeListResponse;
import com.myce.system.dto.fee.RefundFeeRequest;
import com.myce.system.dto.fee.RefundFeeResponse;
import com.myce.system.entity.RefundFeeSetting;
import com.myce.system.entity.type.StandardType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class RefundFeeMapper {

    public RefundFeeSetting toRefundFeeSetting(RefundFeeRequest request) {
        return RefundFeeSetting.builder()
                .name(request.getName())
                .description(request.getDescription())
                .standardType(StandardType.fromString(request.getStandardType()))
                .standardDayCount(request.getStandardDayCount())
                .feeRate(request.getFeeRate())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();
    }

    public RefundFeeListResponse toListResponse(Page<RefundFeeSetting> settings) {
        int currentPage = settings.getNumber() + 1;
        int totalPages = settings.getTotalPages();
        RefundFeeListResponse refundFeeListResponse = new RefundFeeListResponse(currentPage, totalPages);
        settings.forEach(setting -> {
            RefundFeeResponse response = toResponse(setting);
            refundFeeListResponse.addRefundFee(response);
        });

        return refundFeeListResponse;
    }
    
    public RefundFeeListResponse toListResponseFromList(List<RefundFeeSetting> settings) {
        RefundFeeListResponse refundFeeListResponse = new RefundFeeListResponse(1, 1);
        settings.forEach(setting -> {
            RefundFeeResponse response = toResponse(setting);
            refundFeeListResponse.addRefundFee(response);
        });

        return refundFeeListResponse;
    }

    private RefundFeeResponse toResponse(RefundFeeSetting setting) {
        // DB에 저장된 fee_rate가 퍼센트 값(10.00 = 10%)이므로 소수로 변환 (10.00 -> 0.10)
        BigDecimal feeRateDecimal = setting.getFeeRate().divide(BigDecimal.valueOf(100));
        
        return RefundFeeResponse.builder()
                .id(setting.getId())
                .name(setting.getName())
                .description(setting.getDescription())
                .standardType(setting.getStandardType().getDescription())
                .standardDayCount(setting.getStandardDayCount())
                .feeRate(feeRateDecimal)
                .validFrom(setting.getValidFrom())
                .validUntil(setting.getValidUntil())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .isActive(setting.isActive())
                .build();
    }
}