package com.myce.advertisement.service.mapper;

import com.myce.advertisement.dto.AdvertisementRegistrationRequest;
import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.member.entity.Member;

public class AdvertisementRegistrationMapper {
  public static Advertisement toEntity(AdvertisementRegistrationRequest request, Member member, AdPosition adPosition, int totalDays) {
    return Advertisement.builder()
        .member(member)
        .title(request.getTitle())
        .adPosition(adPosition)
        .imageUrl(request.getImageUrl())
        .linkUrl(request.getLinkUrl())
        .description(request.getDescription())
        .displayStartDate(request.getDisplayStartDate())
        .displayEndDate(request.getDisplayEndDate())
        .totalDays(totalDays)
        .status(AdvertisementStatus.PENDING_APPROVAL)
        .build();
  }
}
