package com.myce.system.service.mapper;

import com.myce.system.entity.AdPosition;
import com.myce.system.dto.fee.AdFeeRequest;
import com.myce.system.entity.AdFeeSetting;
import org.springframework.stereotype.Component;

@Component
public class AdFeeMapper {

    public AdFeeSetting getAdFeeSetting(AdFeeRequest request, AdPosition adPosition) {
        return AdFeeSetting.builder()
                .adPosition(adPosition)
                .feePerDay(request.getFeePerDay())
                .isActive(request.getIsActive())
                .build();
    }
}
