package com.myce.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdPositionResponse {
    private Long id;
    private String name;
    @Builder
    public AdPositionResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
