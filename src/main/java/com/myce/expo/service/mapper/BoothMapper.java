package com.myce.expo.service.mapper;

import com.myce.expo.dto.BoothRegistrationRequest;
import com.myce.expo.dto.BoothRegistrationResponse;
import com.myce.expo.entity.Booth;
import com.myce.expo.entity.Expo;
import org.springframework.stereotype.Component;

@Component
public class BoothMapper {

    public Booth toEntity(BoothRegistrationRequest request, Expo expo) {
        return Booth.builder()
                .expo(expo)
                .boothNumber(request.getBoothNumber())
                .name(request.getName())
                .description(request.getDescription())
                .mainImageUrl(request.getMainImageUrl())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .isPremium(request.getIsPremium())
                .displayRank(request.getIsPremium() ? request.getDisplayRank() : 0)
                .build();
    }

    public BoothRegistrationResponse toResponse(Booth booth) {
        return BoothRegistrationResponse.builder()
                .id(booth.getId())
                .boothNumber(booth.getBoothNumber())
                .name(booth.getName())
                .description(booth.getDescription())
                .mainImageUrl(booth.getMainImageUrl())
                .contactName(booth.getContactName())
                .contactPhone(booth.getContactPhone())
                .contactEmail(booth.getContactEmail())
                .isPremium(booth.getIsPremium())
                .displayRank(booth.getDisplayRank())
                .build();
    }
}
