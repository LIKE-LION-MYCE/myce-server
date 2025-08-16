package com.myce.system.dto.adposition;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdPositionDetailResponse {
    private String bannerName;
    private Integer bannerWidth;
    private Integer bannerHeight;
    private Integer maxBannerCount;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public AdPositionDetailResponse(String bannerName, Integer bannerWidth, Integer bannerHeight,
                                    Integer maxBannerCount, boolean isActive, LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {
        this.bannerName = bannerName;
        this.bannerWidth = bannerWidth;
        this.bannerHeight = bannerHeight;
        this.maxBannerCount = maxBannerCount;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
