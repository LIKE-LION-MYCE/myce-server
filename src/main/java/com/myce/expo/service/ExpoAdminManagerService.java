package com.myce.expo.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.ExpoAdminManagerRequest;
import com.myce.expo.dto.ExpoAdminManagerResponse;

import java.util.List;

public interface ExpoAdminManagerService {
    List<ExpoAdminManagerResponse> getMyExpoManagers(Long expoId, Long memberId, LoginType loginType);
    List<ExpoAdminManagerResponse> updateMyExpoManagers(Long expoId,
                                                        Long memberId,
                                                        LoginType loginType,
                                                        List<ExpoAdminManagerRequest> dtos);
}
