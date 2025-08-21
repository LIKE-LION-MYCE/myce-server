package com.myce.system.dto.adposition;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdPositionDropdownWithDimensionsResponse {
    private Long id;
    private String name;
    private Integer bannerWidth;
    private Integer bannerHeight;
    
    @Builder
    public AdPositionDropdownWithDimensionsResponse(Long id, String name, Integer bannerWidth, Integer bannerHeight) {
        this.id = id;
        this.name = name;
        this.bannerWidth = bannerWidth;
        this.bannerHeight = bannerHeight;
    }
}