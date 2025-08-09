package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdRejectInfoResponse {
    private String description;

    @Builder
    public AdRejectInfoResponse(String description) {
        this.description = description;
    }
}
