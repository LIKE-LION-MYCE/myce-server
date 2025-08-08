package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpoAdminManagerResponse {
    private Long id;
    private String adminCode;
    private ExpoAdminManagerPermissionResponse permissions;
}