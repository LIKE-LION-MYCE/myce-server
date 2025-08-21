package com.myce.expo.service.mapper;

import com.myce.expo.dto.ExpoApplicationResponse;
import com.myce.expo.dto.ExpoApplicationDetailResponse;
import com.myce.expo.entity.Expo;
import com.myce.member.entity.Member;

/**
 * 박람회 신청 관련 Entity ↔ DTO 변환 Mapper
 */
public class ExpoApplicationMapper {

    /**
     * Expo Entity를 ExpoApplicationResponse DTO로 변환
     */
    public static ExpoApplicationResponse toSimpleResponse(Expo expo) {
        Member member = expo.getMember();
        
        return ExpoApplicationResponse.builder()
                .id(expo.getId())
                .memberUsername(member.getLoginId())
                .memberName(member.getName())
                .title(expo.getTitle())
                .location(expo.getLocation())
                .memberEmail(member.getEmail())
                .memberPhone(member.getPhone())
                .createdAt(expo.getCreatedAt())
                .statusMessage(expo.getStatus().getLabel())
                .status(expo.getStatus().name())
                .displayStartDate(expo.getDisplayStartDate())
                .build();
    }

    /**
     * Expo Entity를 ExpoApplicationDetailResponse DTO로 변환
     */
    public static ExpoApplicationDetailResponse toDetailResponse(Expo expo, com.myce.common.entity.BusinessProfile businessProfile) {
        Member member = expo.getMember();
        
        return ExpoApplicationDetailResponse.builder()
                .id(expo.getId())
                .title(expo.getTitle())
                .description(expo.getDescription())
                .location(expo.getLocation())
                .locationDetail(expo.getLocationDetail())
                .startDate(expo.getStartDate())
                .endDate(expo.getEndDate())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .startTime(expo.getStartTime())
                .endTime(expo.getEndTime())
                .maxReserverCount(expo.getMaxReserverCount())
                .isPremium(expo.getIsPremium())
                .status(expo.getStatus().name())
                .statusLabel(expo.getStatus().getLabel())
                .thumbnailUrl(expo.getThumbnailUrl())
                .category(expo.getExpoCategories().stream()
                        .map(expoCategory -> expoCategory.getCategory().getName())
                        .reduce((first, second) -> first + ", " + second)
                        .orElse("카테고리 없음"))
                .createdAt(expo.getCreatedAt())
                .applicant(ExpoApplicationDetailResponse.ApplicantInfo.builder()
                        .memberId(member.getId())
                        .loginId(member.getLoginId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .phone(member.getPhone())
                        .birth(member.getBirth())
                        .build())
                .business(businessProfile != null ? ExpoApplicationDetailResponse.BusinessInfo.builder()
                        .companyName(businessProfile.getCompanyName())
                        .ceoName(businessProfile.getCeoName())
                        .address(businessProfile.getAddress())
                        .contactPhone(businessProfile.getContactPhone())
                        .contactEmail(businessProfile.getContactEmail())
                        .businessRegistrationNumber(businessProfile.getBusinessRegistrationNumber())
                        .build() : null)
                .build();
    }
}