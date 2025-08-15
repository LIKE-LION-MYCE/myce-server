package com.myce.expo.dto;

import com.myce.expo.entity.type.ExpoStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class ExpoBasicResponse {
    private Long expoId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private ExpoStatus status;
    
    // 일정 정보
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    
    // 기본 장소 정보
    private String location;
    private String locationDetail;
    
    // 수용 정보
    private Integer maxReserverCount;
    private Integer currentReservationCount;
    
    // 주최자 정보
    private String organizerName;
    private String organizerContact;
    private OrganizerInfo organizerInfo;
    
    // 카테고리
    private List<String> categories;
    
    @Getter
    @Builder
    public static class OrganizerInfo {
        private String companyName;
        private String ceoName;
        private String contactPhone;
        private String contactEmail;
        private String address;
        private String businessRegistrationNumber;
    }
}