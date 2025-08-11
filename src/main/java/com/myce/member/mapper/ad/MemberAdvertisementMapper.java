package com.myce.member.mapper.ad;

import com.myce.advertisement.entity.Advertisement;
import com.myce.member.dto.ad.MemberAdvertisementResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MemberAdvertisementMapper {
    
    public MemberAdvertisementResponse toResponseDto(Advertisement advertisement) {
        return MemberAdvertisementResponse.builder()
                .advertisementId(advertisement.getId())
                .title(advertisement.getTitle())
                .displayStartDate(advertisement.getDisplayStartDate())
                .displayEndDate(advertisement.getDisplayEndDate())
                .status(advertisement.getStatus())
                .adPositionId(advertisement.getAdPosition().getId())
                .adPositionName(advertisement.getAdPosition().getName())
                .build();
    }
    
    public List<MemberAdvertisementResponse> toResponseDtoList(List<Advertisement> advertisements) {
        return advertisements.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}