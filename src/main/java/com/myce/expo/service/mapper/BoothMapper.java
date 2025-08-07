package com.myce.expo.service.mapper;

import com.myce.expo.dto.BoothRequest;
import com.myce.expo.dto.BoothResponse;
import com.myce.expo.entity.Booth;
import com.myce.expo.entity.Expo;
import org.springframework.stereotype.Component;

@Component
public class BoothMapper {

    public Booth toEntity(BoothRequest request, Expo expo) {
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

    public BoothResponse toResponse(Booth booth) {
        return BoothResponse.builder()
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