package com.myce.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {

    @NotNull(message = "환불 금액은 필수입니다.")
    @Positive(message = "환불 금액은 0보다 커야 합니다.")
    private Integer amount;

    @NotBlank(message = "환불 사유는 필수입니다.")
    private String reason;
}