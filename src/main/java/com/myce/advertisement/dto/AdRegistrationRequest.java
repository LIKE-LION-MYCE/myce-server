package com.myce.advertisement.dto;

import com.myce.common.dto.RegistrationCompanyRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdRegistrationRequest {
  @NotBlank(message = "광고명은 필수입니다.")
  @Size(max = 100, message = "광고명은 100자 이하여야 합니다.")
  private String title;

  @NotNull(message = "광고 배너 위치는 필수입니다.")
  private Long adPositionId;

  @NotBlank(message = "광고 이미지 URL은 필수입니다.")
  @Size(max = 500, message = "광고 이미지 URL 길이는 500자 이하여야 합니다.")
  private String imageUrl;

  @NotBlank(message = "광고 링크 URL은 필수입니다.")
  @Size(max = 500, message = "광고 링크 URL 길이는 500자 이하여야 합니다.")
  private String linkUrl;

  @NotBlank(message = "광고 상세 소개는 필수입니다.")
  private String description;

  @NotNull(message = "광고 시작일은 필수입니다.")
  private LocalDate displayStartDate;

  @NotNull(message = "광고 종료일은 필수입니다.")
  private LocalDate displayEndDate;

  @NotNull(message = "회사 정보는 필수입니다.")
  private RegistrationCompanyRequest registrationCompanyRequest;
}
