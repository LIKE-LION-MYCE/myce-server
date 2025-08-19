package com.myce.advertisement.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdDetailResponse {

    private Long id;
    private String statusMessage;
    private String bannerImageUrl;
    private String title;
    private String bannerLocationName;
    private LocalDate startAt;
    private LocalDate endAt;
    private String description;
    private ApplicantInfo applicant;
    private String businessCompany;
    private String representName;
    private String businessEmail;
    private String businessPhone;
    private String address;
    private String businessNumber;

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

}
