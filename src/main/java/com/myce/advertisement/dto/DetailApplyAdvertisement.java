package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
public class DetailApplyAdvertisement {
    private Long id;
    private String statusMessage;
    private String bannerImageUrl;
    private String title;
    private String bannerLocationName;
    private LocalDate startAt;
    private LocalDate endAt;
    private String description;
    private String businessCompany;
    private String representName;
    private String businessEmail;
    private String businessPhone;
    private String address;
    private String businessNumber;

}
