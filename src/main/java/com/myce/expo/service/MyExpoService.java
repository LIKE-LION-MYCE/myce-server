package com.myce.expo.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.ExpoAdminPermissionResponse;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoDescriptionUpdateRequest;

public interface MyExpoService {
    MyExpoDetailResponse getMyExpoDetail(Long expoId, LoginType loginType, Long principalId);
    MyExpoDetailResponse updateMyExpoDescription(Long expoId, MyExpoDescriptionUpdateRequest updateRequest, LoginType loginType, Long principalId);
    ExpoAdminPermissionResponse getExpoAdminPermission(Long memberId, LoginType loginType);
}