package com.myce.system.dto.fee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class ExpoFeeRequest {

    @NotBlank(message = "요금제명을 입력해주세요.")
    private String name;

    @NotNull(message = "등록금을 입력해주세요.")
    @Min(value = 0, message = "등록금은 0원 이상 입력되어야 합니다.")
    private Integer deposit;

    @NotNull(message = "프리미엄 서비스 요금을 입력해주세요.")
    @Min(value = 0, message = "프리미엄 서비스 요금은 0원 이상 입력되어야 합니다. ")
    private Integer premiumDeposit;

    @NotNull(message = "정산 수수료를 입력해주세요.")
    @Min(value = 0, message = "정산 수수료는 0 이상 입력되어야 합니다. ")
    private BigDecimal settlementCommission;

    @NotNull(message = "일일 사용료를 입력해주세요.")
    @Min(value = 0, message = "일일 사용료는 0 이상 입력되어야 합니다. ")
    private Integer dailyUsageFee;

    @NotNull(message = "활성화 여부를 입력해주세요.")
    private Boolean isActive;
}
