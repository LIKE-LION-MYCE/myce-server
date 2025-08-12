package com.myce.system.dto.fee;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeeActiveRequest {

    @NotNull(message = "활성화 여부를 입력해주세요.")
    private Boolean isActive;
}
