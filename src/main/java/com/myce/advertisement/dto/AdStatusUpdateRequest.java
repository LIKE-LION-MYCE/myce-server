package com.myce.advertisement.dto;

import com.myce.advertisement.entity.type.AdvertisementStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdStatusUpdateRequest {
  private AdvertisementStatus advertisementStatus;
}
