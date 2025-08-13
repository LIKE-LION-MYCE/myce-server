package com.myce.expo.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.ExpoAdminPermissionResponse;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoUpdateRequest;

public interface MyExpoService {
    MyExpoDetailResponse getMyExpoDetail(Long expoId, LoginType loginType, Long principalId);
    MyExpoDetailResponse updateMyExpoDetail(Long expoId, MyExpoUpdateRequest updateRequest, LoginType loginType, Long principalId);
    ExpoAdminPermissionResponse getExpoAdminPermission(Long memberId, LoginType loginType);
}