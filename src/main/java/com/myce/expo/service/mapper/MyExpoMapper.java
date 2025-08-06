package com.myce.expo.service.mapper;

import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// Entity와 DTO 간의 변환을 담당하는 매퍼 클래스
@Component
public class MyExpoMapper {

    // Expo 엔티티와 ExpoCategory 리스트를 MyExpoDetailResponse DTO로 변환
    public MyExpoDetailResponse toMyExpoDetailResponse(Expo expo, List<ExpoCategory> expoCategories) {
        return MyExpoDetailResponse.builder()
                .id(expo.getId())
                .categoryIds(expoCategories.stream()
                        .map(expoCategory -> expoCategory.getCategory().getId())
                        .collect(Collectors.toList()))
                .title(expo.getTitle())
                .thumbnailUrl(expo.getThumbnailUrl())
                .description(expo.getDescription())
                .location(expo.getLocation())
                .locationDetail(expo.getLocationDetail())
                .maxReserverCount(expo.getMaxReserverCount())
                .startDate(expo.getStartDate())
                .endDate(expo.getEndDate())
                .status(expo.getStatus())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .startTime(expo.getStartTime())
                .endTime(expo.getEndTime())
                .isPremium(expo.getIsPremium())
                .build();
    }
}
