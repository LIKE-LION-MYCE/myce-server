package com.myce.advertisement.service.mapper;

import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.BusinessProfile;
import com.myce.member.entity.Member;

public class AdvertisementMapper {
    public static SimpleApplyAdvertisement getSimpleAdvertisement(Advertisement advertisement,
              BusinessProfile businessProfile) {
        Member member = advertisement.getMember();
        AdPosition adPosition = advertisement.getAdPosition();

        return SimpleApplyAdvertisement.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .memberUsername(member.getLoginId())
                .memberNickname(member.getName())
                .memberEmail(member.getEmail())
                .bannerLocationName(adPosition.getName())
                .createdAt(advertisement.getCreatedAt())
                .memberPhone(businessProfile.getContactPhone())
                .statusMessage(advertisement.getStatus().name())
                .build();
    }

    public static DetailApplyAdvertisement getDetailAdvertisement(Advertisement advertisement,
              BusinessProfile businessProfile) {
        AdPosition adPosition = advertisement.getAdPosition();

        return DetailApplyAdvertisement.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .statusMessage(advertisement.getStatus().name())
                .bannerLocationName(adPosition.getName())
                .bannerImageUrl(advertisement.getImageUrl())
                .startAt(advertisement.getDisplayStartDate())
                .endAt(advertisement.getDisplayEndDate())
                .description(advertisement.getDescription())
                .businessCompany(businessProfile.getCompanyName())
                .representName(businessProfile.getCeoName())
                .businessEmail(businessProfile.getContactEmail())
                .businessPhone(businessProfile.getContactPhone())
                .address(businessProfile.getAddress())
                .businessNumber(businessProfile.getBusinessRegistrationNumber())
                .build();
    }

}
