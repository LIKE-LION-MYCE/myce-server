package com.myce.system.service.mapper;

import com.myce.system.entity.AdPosition;
import com.myce.system.dto.fee.AdFeeListResponse;
import com.myce.system.dto.fee.AdFeeRequest;
import com.myce.system.dto.fee.AdFeeResponse;
import com.myce.system.entity.AdFeeSetting;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class AdFeeMapper {

    public AdFeeSetting getAdFeeSetting(AdFeeRequest request, AdPosition adPosition) {
        return AdFeeSetting.builder()
                .adPosition(adPosition)
                .name(request.getName())
                .feePerDay(request.getFeePerDay())
                .isActive(request.getIsActive())
                .build();
    }

    public AdFeeListResponse toListResponse(Page<AdFeeSetting> adFeeSettingList) {
        int currentPage = adFeeSettingList.getNumber() + 1;
        int totalPages = adFeeSettingList.getTotalPages();
        AdFeeListResponse adFeeListResponse = new AdFeeListResponse(currentPage, totalPages);
        adFeeSettingList.forEach(adFeeSetting -> {
            AdFeeResponse response = toAdFeeResponse(adFeeSetting);
            adFeeListResponse.addAdFee(response);
        });
        return adFeeListResponse;
    }

    public AdFeeResponse toAdFeeResponse(AdFeeSetting adFeeSetting) {
        return AdFeeResponse.builder()
                .id(adFeeSetting.getId())
                .position(adFeeSetting.getAdPosition().getName())
                .name(adFeeSetting.getName())
                .feePerDay(adFeeSetting.getFeePerDay())
                .isActive(adFeeSetting.getIsActive())
                .createdAt(adFeeSetting.getCreatedAt())
                .updatedAt(adFeeSetting.getUpdatedAt())
                .build();

    }
}
