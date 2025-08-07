package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainPageAdInfo {
    private Long bannerId;
    private Long locationId;
    private String bannerImageUrl;
    private String linkUrl;
}
