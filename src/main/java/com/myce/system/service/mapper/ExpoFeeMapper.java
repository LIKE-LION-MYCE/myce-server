package com.myce.system.service.mapper;

import com.myce.system.dto.fee.ExpoFeeRequest;
import com.myce.system.entity.ExpoFeeSetting;
import org.springframework.stereotype.Component;

@Component
public class ExpoFeeMapper {

    public ExpoFeeSetting toExpoFeeSetting(ExpoFeeRequest request) {
        return ExpoFeeSetting.builder()
                .deposit(request.getDeposit())
                .premiumDeposit(request.getPremiumDeposit())
                .settlementCommission(request.getSettlementCommission())
                .dailyUsageFee(request.getDailyUsageFee())
                .isActive(request.getIsActive())
                .build();
    }

}
