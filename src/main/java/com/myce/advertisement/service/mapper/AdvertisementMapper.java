package com.myce.advertisement.service.mapper;

import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.BusinessProfile;
import com.myce.member.entity.Member;

public class AdvertisementMapper {
    public static SimpleApplyAdvertisement getSimpleAdvertisement(Advertisement advertisement, BusinessProfile businessProfile) {
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
}
