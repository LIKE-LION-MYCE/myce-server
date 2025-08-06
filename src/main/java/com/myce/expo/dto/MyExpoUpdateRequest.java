package com.myce.expo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 나의 박람회 수정 요청 DTO
@Getter
@NoArgsConstructor
public class MyExpoUpdateRequest {

    // 박람회에 연결할 카테고리 ID 리스트 TODO: NotBlank 추후 추가
    private List<Long> categoryIds;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
    private String title;

    @Size(max = 500, message = "썸네일 URL은 500자 이하로 입력해주세요.")
    private String thumbnailUrl;

    @NotBlank(message = "상세 설명은 필수 입력 값입니다.")
    private String description;

    @NotBlank(message = "위치는 필수 입력 값입니다.")
    @Size(max = 100, message = "위치는 100자 이하로 입력해주세요.")
    private String location;

    @NotBlank(message = "세부 위치는 필수 입력 값입니다.")
    @Size(max = 100, message = "세부 위치는 100자 이하로 입력해주세요.")
    private String locationDetail;

    @NotNull(message = "최대 수용 인원수는 필수 입력 값입니다.")
    @PositiveOrZero(message = "최대 수용 인원수는 0 이상이어야 합니다.")
    private Integer maxReserverCount;

    @NotNull(message = "개최 시작일은 필수 입력 값입니다.")
    private LocalDate startDate;

    @NotNull(message = "개최 종료일은 필수 입력 값입니다.")
    private LocalDate endDate;

    @NotNull(message = "게시 시작일은 필수 입력 값입니다.")
    private LocalDate displayStartDate;

    @NotNull(message = "게시 종료일은 필수 입력 값입니다.")
    private LocalDate displayEndDate;

    @NotNull(message = "운영 시작 시간은 필수 입력 값입니다.")
    private LocalTime startTime;

    @NotNull(message = "운영 종료 시간은 필수 입력 값입니다.")
    private LocalTime endTime;

    @NotNull(message = "부스 프리미엄 여부는 필수 입력 값입니다.")
    private Boolean isPremium;

    @Builder
    public MyExpoUpdateRequest(List<Long> categoryIds, String title, String thumbnailUrl, String description,
                               String location, String locationDetail, Integer maxReserverCount,
                               LocalDate startDate, LocalDate endDate,
                               LocalDate displayStartDate, LocalDate displayEndDate,
                               LocalTime startTime, LocalTime endTime, Boolean isPremium) {
        this.categoryIds = categoryIds;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.location = location;
        this.locationDetail = locationDetail;
        this.maxReserverCount = maxReserverCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isPremium = isPremium;
    }
}
