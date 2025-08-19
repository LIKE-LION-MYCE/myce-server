package com.myce.advertisement.service.mapper;

import com.myce.advertisement.dto.*;
import com.myce.system.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.BusinessProfile;
import com.myce.member.entity.Member;

public class AdMapper {
    public static AdResponse getSimpleAdvertisement(
            Advertisement advertisement, BusinessProfile businessProfile) {
        Member member = advertisement.getMember();
        AdPosition adPosition = advertisement.getAdPosition();

        return AdResponse.builder()
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

    public static AdDetailResponse getDetailAdvertisement(
            Advertisement advertisement, BusinessProfile businessProfile) {
        AdPosition adPosition = advertisement.getAdPosition();
        Member member = advertisement.getMember();

        return AdDetailResponse.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .statusMessage(advertisement.getStatus().name())
                .bannerLocationName(adPosition.getName())
                .bannerImageUrl(advertisement.getImageUrl())
                .startAt(advertisement.getDisplayStartDate())
                .endAt(advertisement.getDisplayEndDate())
                .description(advertisement.getDescription())
                .applicant(AdDetailResponse.ApplicantInfo.builder()
                        .name(member.getName())
                        .birth(member.getBirth())
                        .email(member.getEmail())
                        .gender(member.getGender().name())
                        .phone(member.getPhone())
                        .loginId(member.getLoginId())
                        .build())
                .businessCompany(businessProfile.getCompanyName())
                .representName(businessProfile.getCeoName())
                .businessEmail(businessProfile.getContactEmail())
                .businessPhone(businessProfile.getContactPhone())
                .address(businessProfile.getAddress())
                .businessNumber(businessProfile.getBusinessRegistrationNumber())
                .build();
    }
}
