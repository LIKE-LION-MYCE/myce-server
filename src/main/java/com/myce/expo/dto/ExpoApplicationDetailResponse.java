package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
@Getter
public class ExpoApplicationDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private String locationDetail;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxReserverCount;
    private Boolean isPremium;
    private String status;
    private String statusLabel;
    private String thumbnailUrl;
    private String category;
    private LocalDateTime createdAt;
    private ApplicantInfo applicant;
    private BusinessInfo business;

    @Builder
    @Getter
    public static class ApplicantInfo {
        private Long memberId;
        private String loginId;
        private String name;
        private String email;
        private String phone;
        private String gender;
        private LocalDate birth;
    }

    @Builder
    @Getter
    public static class BusinessInfo {
        private String companyName;
        private String ceoName;
        private String address;
        private String contactPhone;
        private String contactEmail;
        private String businessRegistrationNumber;
    }
}