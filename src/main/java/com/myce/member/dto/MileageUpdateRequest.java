package com.myce.member.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MileageUpdateRequest {
  @NotNull
  @Min(0)
  private Integer usedMileage;

  @NotNull
  @Min(0)
  private Integer savedMileage;

  public MileageUpdateRequest(Integer usedMileage, Integer savedMileage) {
  }
}
