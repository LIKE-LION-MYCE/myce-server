package com.myce.system.service.mapper;

import com.myce.system.dto.fee.RefundFeeListResponse;
import com.myce.system.dto.fee.RefundFeeResponse;
import com.myce.system.entity.RefundFeeSetting;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class RefundFeeMapper {

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

    private RefundFeeResponse toResponse(RefundFeeSetting setting) {
        return RefundFeeResponse.builder()
                .id(setting.getId())
                .name(setting.getName())
                .description(setting.getDescription())
                .standardType(setting.getStandardType().getDescription())
                .standardDayCount(setting.getStandardDayCount())
                .feeRate(setting.getFeeRate())
                .validFrom(setting.getValidFrom())
                .validUntil(setting.getValidUntil())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}