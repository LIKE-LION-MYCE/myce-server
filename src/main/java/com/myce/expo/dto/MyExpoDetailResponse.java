package com.myce.expo.dto;

import com.myce.expo.entity.type.ExpoStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime; // LocalTime import 추가
import java.util.List;

// 나의 박람회 상세 조회 응답 DTO
@Getter
@NoArgsConstructor
public class MyExpoDetailResponse {

    private Long id;
    // 박람회에 연결된 카테고리 ID 리스트
    private List<Long> categoryIds;
    private String title;
    private String thumbnailUrl;
    private String description;
    private String location;
    private String locationDetail;
    private Integer maxReserverCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExpoStatus status;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isPremium;

    @Builder
    public MyExpoDetailResponse(Long id, List<Long> categoryIds, String title, String thumbnailUrl, String description,
                                String location, String locationDetail, Integer maxReserverCount, LocalDate startDate,
                                LocalDate endDate, ExpoStatus status, LocalDate displayStartDate,
                                LocalDate displayEndDate, LocalTime startTime, LocalTime endTime, Boolean isPremium) {
        this.id = id;
        this.categoryIds = categoryIds;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.location = location;
        this.locationDetail = locationDetail;
        this.maxReserverCount = maxReserverCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isPremium = isPremium;
    }
}
