package com.myce.system.service.mapper;

import com.myce.system.dto.fee.ExpoFeeListResponse;
import com.myce.system.dto.fee.ExpoFeeRequest;
import com.myce.system.dto.fee.ExpoFeeResponse;
import com.myce.system.entity.ExpoFeeSetting;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ExpoFeeMapper {

    public ExpoFeeSetting toExpoFeeSetting(ExpoFeeRequest request) {
        return ExpoFeeSetting.builder()
                .name(request.getName())
                .deposit(request.getDeposit())
                .premiumDeposit(request.getPremiumDeposit())
                .settlementCommission(request.getSettlementCommission())
                .dailyUsageFee(request.getDailyUsageFee())
                .isActive(request.getIsActive())
                .build();
    }

    public ExpoFeeListResponse toListResponse(Page<ExpoFeeSetting> expoFeeSettings) {
        int currentPage = expoFeeSettings.getNumber() + 1;
        int totalPages = expoFeeSettings.getTotalPages();
        ExpoFeeListResponse expoFeeListResponse = new ExpoFeeListResponse(currentPage, totalPages);
        expoFeeSettings.forEach(expoFeeSetting -> {
            ExpoFeeResponse response = toResponse(expoFeeSetting);
            expoFeeListResponse.addExpoFee(response);
        });
        return expoFeeListResponse;
    }

    public ExpoFeeResponse toResponse(ExpoFeeSetting expoFeeSetting) {
        return ExpoFeeResponse.builder()
                .id(expoFeeSetting.getId())
                .name(expoFeeSetting.getName())
                .deposit(expoFeeSetting.getDeposit())
                .premiumDeposit(expoFeeSetting.getPremiumDeposit())
                .settlementCommission(expoFeeSetting.getSettlementCommission())
                .dailyUsageFee(expoFeeSetting.getDailyUsageFee())
                .isActive(expoFeeSetting.getIsActive())
                .createTime(expoFeeSetting.getCreatedAt())
                .updateTime(expoFeeSetting.getUpdatedAt())
                .build();
    }

}
