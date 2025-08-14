package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExpoCardResponse {
  private Long expoId;
  private String title;
  private String startDate;
  private String endDate;
  private String location;
  private String locationDetail;
  private String thumbnailUrl;
  private Integer remainingQuantity;
  private boolean isBookmark;
}
