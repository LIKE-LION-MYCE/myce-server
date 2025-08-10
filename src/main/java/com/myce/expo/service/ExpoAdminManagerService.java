package com.myce.expo.service;

import com.myce.expo.dto.ExpoAdminManagerRequest;
import com.myce.expo.dto.ExpoAdminManagerResponse;

import java.util.List;

public interface ExpoAdminManagerService {
    List<ExpoAdminManagerResponse> getMyExpoManagers(Long expoId, Long memberId);
    List<ExpoAdminManagerResponse> updateMyExpoManagers(Long expoId, Long memberId, List<ExpoAdminManagerRequest> dtos);
}
