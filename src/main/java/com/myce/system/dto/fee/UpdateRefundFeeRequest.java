package com.myce.system.dto.fee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRefundFeeRequest {

    @NotBlank(message = "요금제명을 입력해주세요.")
    private String name;
    @NotBlank(message = "요금제 설명을 입력해주세요.")
    private String description;
    @NotNull(message = "기준 날짜 수를 입력해주세요.")
    @Min(value = 0, message = "기준 날짜는 양수로만 입력 가능합니다.")
    private int standardDayCount;
    @NotNull(message = "수수료율을 입력해주세요.")
    private BigDecimal feeRate;
    @NotNull(message = "요금제 시작 날짜를 입력해주세요.")
    private LocalDateTime validFrom;
    @NotNull(message = "요금제 종료 날짜를 입력해주세요.")
    private LocalDateTime validUntil;
}
