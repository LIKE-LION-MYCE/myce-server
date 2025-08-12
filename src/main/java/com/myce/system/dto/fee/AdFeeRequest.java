package com.myce.system.dto.fee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdFeeRequest {
    @NotNull(message = "광고 위치를 입력해주세요.")
    private Long positionId;
    @NotBlank(message = "요금설정명을 입력해주세요.")
    private String name;
    @NotNull(message = "이용료를 입력해주세요.")
    @Min(value = 0, message = "이용료는 0원 이상 입력되어야 합니다.")
    private Integer feePerDay;
    @NotNull(message = "활성화 여부를 입력해주세요.")
    private Boolean isActive;
}
