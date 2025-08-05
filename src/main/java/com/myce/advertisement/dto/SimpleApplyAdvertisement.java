package com.myce.advertisement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class SimpleApplyAdvertisement {
    private Long id;
    private String memberUsername;
    private String memberNickname;
    private String title;
    private String bannerLocationName;
    private String memberEmail;
    private String memberPhone;
    private LocalDateTime createdAt;
    private String statusMessage;

}
