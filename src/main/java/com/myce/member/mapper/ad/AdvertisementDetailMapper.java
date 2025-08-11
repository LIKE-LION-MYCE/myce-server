package com.myce.member.mapper.ad;

import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.BusinessProfile;
import com.myce.member.dto.ad.AdvertisementDetailResponse;
import org.springframework.stereotype.Component;

@Component
public class AdvertisementDetailMapper {
    
    public AdvertisementDetailResponse toResponseDto(Advertisement advertisement, BusinessProfile businessProfile) {
        return AdvertisementDetailResponse.builder()
                .advertisementId(advertisement.getId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .imageUrl(advertisement.getImageUrl())
                .linkUrl(advertisement.getLinkUrl())
                .displayStartDate(advertisement.getDisplayStartDate())
                .displayEndDate(advertisement.getDisplayEndDate())
                .status(advertisement.getStatus())
                .adPositionName(advertisement.getAdPosition().getName())
                .businessInfo(buildBusinessInfo(businessProfile))
                .build();
    }
    
    private AdvertisementDetailResponse.BusinessInfo buildBusinessInfo(BusinessProfile businessProfile) {
        return AdvertisementDetailResponse.BusinessInfo.builder()
                .companyName(businessProfile.getCompanyName())
                .ceoName(businessProfile.getCeoName())
                .contactPhone(businessProfile.getContactPhone())
                .businessRegistrationNumber(businessProfile.getBusinessRegistrationNumber())
                .build();
    }
}