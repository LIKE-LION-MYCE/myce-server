package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpoBookmarkResponse {
    private Long expoId;
    private String expoTitle;
    private Boolean isBookmarked; // 현재 사용자의 찜 상태
}