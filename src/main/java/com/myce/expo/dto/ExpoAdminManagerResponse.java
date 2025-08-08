package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExpoAdminManagerResponse {
    private String adminCode;
    private List<ExpoAdminManagerPermissionResponse> permissions;
}
