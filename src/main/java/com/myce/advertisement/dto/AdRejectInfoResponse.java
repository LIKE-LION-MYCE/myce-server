package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdRejectInfoResponse {
    private String description;
    private LocalDateTime rejectedAt;

    @Builder
    public AdRejectInfoResponse(String description, LocalDateTime rejectedAt) {
        this.description = description;
        this.rejectedAt = rejectedAt;
    }
}
