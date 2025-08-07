package com.myce.advertisement.service.mapper;

import com.myce.advertisement.dto.AdPositionResponse;
import com.myce.advertisement.entity.AdPosition;

public class AdPositionMapper {
  public static AdPositionResponse toDto(AdPosition adPosition) {
    return new AdPositionResponse(adPosition.getId(), adPosition.getName());
  }
}
