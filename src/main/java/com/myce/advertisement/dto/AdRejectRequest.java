package com.myce.advertisement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdRejectRequest {
    @NotBlank(message = "거절 사유는 빈칸이 되면 안됩니다.")
    private String reason;

    @Builder
    public AdRejectRequest(String reason) {
        this.reason = reason;
    }

}
