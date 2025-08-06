package com.myce.expo.dto;

import com.myce.expo.entity.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpoRegistrationRequest {
  @NotBlank(message = "박람회 포스터 URL은 필수입니다.")
  @Size(max = 500, message = "박람회 포스터 URL 길이는 500자 이하여야 합니다.")
  private String thumbnailUrl;

  @NotBlank(message = "박람회 이름은 필수입니다.")
  @Size(max = 100, message = "박람회 이름은 100자 이하여야 합니다.")
  private String title;

  @NotNull(message = "박람회 개최 시작일은 필수입니다.")
  private LocalDate startDate;

  @NotNull(message = "박람회 개최 종료일은 필수입니다.")
  private LocalDate endDate;

  @NotNull(message = "박람회 게시 시작일은 필수입니다.")
  private LocalDate displayStartDate;

  @NotNull(message = "박람회 게시 종료일은 필수입니다.")
  private LocalDate displayEndDate;

  @NotBlank(message = "박람회 장소는 필수입니다.")
  @Size(max = 100, message = "박람회 장소는 100자 이하여야 합니다.")
  private String location;

  @NotBlank(message = "박람회 세부 장소는 필수입니다.")
  @Size(max = 100, message = "박람회 세부 장소는 100자 이하여야 합니다.")
  private String locationDetail;

  @NotNull(message = "박람회 좌표(위도)는 필수입니다.")
  private BigDecimal latitude;

  @NotNull(message = "박람회 좌표(경도)는 필수입니다.")
  private BigDecimal longitude;

  @NotNull(message = "운영 시작 시간은 필수입니다.")
  private LocalTime startTime;

  @NotNull(message = "운영 종료 시간은 필수입니다.")
  private LocalTime endTime;

  @NotNull(message = "최대 수용 인원은 필수입니다.")
  @Min(value = 1, message = "최대 수용 인원은 1명 이상이어야 합니다.")
  private Integer maxReserverCount;

  @NotBlank(message = "박람회 상세 소개는 필수입니다.")
  private String description;

  @NotEmpty(message = "카테고리는 1개 이상 선택해야 합니다.")
  private List<Long> categoryIds;

  @NotNull(message = "프리미엄 여부는 필수입니다.")
  private Boolean isPremium;

  @NotNull(message = "회사 정보는 필수입니다.")
  private ExpoRegistrationCompanyRequest expoRegistrationCompanyRequest;
}
