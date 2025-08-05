package com.myce.advertisement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterRequest {
    @Min(value = 0, message = "page는 0 이상이어야 합니다")
    private int page = 0;

    private String keyword = "";

    private String status;

}