package com.myce.expo.service.mapper;

import com.myce.expo.dto.ExpoCardResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class ExpoMapper {
  public static Expo toEntity(ExpoRegistrationRequest request, Member member){
    return Expo.builder()
          .member(member)
          .title(request.getTitle())
          .thumbnailUrl(request.getThumbnailUrl())
          .description(request.getDescription())
          .location(request.getLocation())
          .locationDetail(request.getLocationDetail())
          .latitude(request.getLatitude())
          .longitude(request.getLongitude())
          .startDate(request.getStartDate())
          .endDate(request.getEndDate())
          .displayStartDate(request.getDisplayStartDate())
          .displayEndDate(request.getDisplayEndDate())
          .startTime(request.getStartTime())
          .endTime(request.getEndTime())
          .status(ExpoStatus.PENDING_APPROVAL) // 맨 첫 등록은 승인 대기로
          .isPremium(request.getIsPremium())
          .maxReserverCount(request.getMaxReserverCount())
          .build();
  }

  public static ExpoCardResponse toCards(Expo expo, Integer remainingQuantity, boolean isBookmark){
    return ExpoCardResponse.builder()
        .expoId(expo.getId())
        .title(expo.getTitle())
        .startDate(expo.getStartDate().toString())
        .endDate(expo.getEndDate().toString())
        .location(expo.getLocation())
        .locationDetail(expo.getLocationDetail())
        .thumbnailUrl(expo.getThumbnailUrl())
        .remainingQuantity(remainingQuantity)
        .isBookmark(isBookmark)
        .status(expo.getStatus().name())
        .build();
  }
}
