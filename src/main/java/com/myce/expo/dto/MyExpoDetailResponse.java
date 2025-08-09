package com.myce.expo.dto;

import com.myce.expo.entity.type.ExpoStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 나의 박람회 상세 조회 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

}
