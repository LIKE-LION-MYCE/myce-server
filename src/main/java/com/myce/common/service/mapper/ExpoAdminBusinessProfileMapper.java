package com.myce.common.service.mapper;

import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;
import com.myce.common.entity.BusinessProfile;
import org.springframework.stereotype.Component;

@Component
public class ExpoAdminBusinessProfileMapper {
    public ExpoAdminBusinessProfileResponseDto toDto(BusinessProfile profile) {
        return ExpoAdminBusinessProfileResponseDto.builder()
                .logoUrl(profile.getLogoUrl())
                .companyName(profile.getCompanyName())
                .ceoName(profile.getCeoName())
                .contactEmail(profile.getContactEmail())
                .contactPhone(profile.getContactPhone())
                .address(profile.getAddress())
                .businessRegistrationNumber(profile.getBusinessRegistrationNumber())
                .build();
    }
}
