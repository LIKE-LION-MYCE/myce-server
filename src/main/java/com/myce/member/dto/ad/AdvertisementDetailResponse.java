package com.myce.member.dto.ad;

import com.myce.advertisement.entity.type.AdvertisementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertisementDetailResponse {
    
    // 광고 기본 정보
    private Long advertisementId;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private AdvertisementStatus status;
    
    // 광고 위치 정보
    private String adPositionName;
    
    // 사업자 정보
    private BusinessInfo businessInfo;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessInfo {
        private String companyName;
        private String ceoName;
        private String contactPhone;
        private String businessRegistrationNumber;
    }
}