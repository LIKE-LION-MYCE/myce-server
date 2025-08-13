package com.myce.system.service.mapper;

import com.myce.common.dto.PageResponse;
import com.myce.system.dto.adposition.AdPositionDetailResponse;
import com.myce.system.dto.adposition.AdPositionDropdownResponse;
import com.myce.system.dto.adposition.AdPositionNewRequest;
import com.myce.system.dto.adposition.AdPositionResponse;
import com.myce.system.entity.AdPosition;
import org.springframework.data.domain.Page;

public class AdPositionMapper {
    public static AdPositionDropdownResponse toDto(AdPosition adPosition) {
        return new AdPositionDropdownResponse(adPosition.getId(), adPosition.getName());
    }

    public static Page<AdPositionResponse> toListDto(Page<AdPosition> adPositions) {
        return adPositions.map(
                adPosition -> AdPositionResponse.builder()
                        .id(adPosition.getId())
                        .name(adPosition.getName())
                        .createdAt(adPosition.getCreatedAt())
                        .updatedAt(adPosition.getUpdatedAt())
                        .isActive(adPosition.getIsActive())
                        .build()
        );
    }

    public static AdPositionDetailResponse toDetailDto(AdPosition position){
        return AdPositionDetailResponse.builder()
                .bannerName(position.getName())
                .bannerWidth(position.getImageWidth())
                .bannerHeight(position.getImageHeight())
                .maxBannerCount(position.getMaxCount())
                .isActive(position.getIsActive())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }

    public static AdPosition toEntity(AdPositionNewRequest request){
        return AdPosition.builder()
                .name(request.getBannerName())
                .imageWidth(request.getBannerWidth())
                .imageHeight(request.getBannerHeight())
                .maxCount(request.getMaxBannerCount())
                .isActive(request.isActive())
                .build();
    }
}
