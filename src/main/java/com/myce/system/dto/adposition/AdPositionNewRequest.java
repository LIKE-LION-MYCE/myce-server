package com.myce.system.dto.adposition;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdPositionNewRequest {
    @NotBlank(message = "배너 이름을 입력해 주세요.")
    private String bannerName;
    @NotNull(message = "배너 너비를 입력해 주세요.")
    @Min(value = 0, message = "배너 너비는 음수가 되면 안됩니다.")
    private Integer bannerWidth;
    @NotNull(message = "배너 높이를 입력해 주세요.")
    @Min(value = 0, message = "배너 높이는 음수가 되면 안됩니다.")
    private Integer bannerHeight;
    @NotNull(message = "총 배너 수를 입력해 주세요.")
    @Min(value = 0, message = "총 배너 수는 음수가 되면 안됩니다.")
    private Integer maxBannerCount;
    private boolean active;
}
