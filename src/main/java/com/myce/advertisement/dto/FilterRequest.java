package com.myce.advertisement.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FilterRequest {
    @Min(value = 0, message = "page는 0 이상이어야 합니다")
    private int page;

    private String keyword = "";

    private String status;

}