package com.myce.common.service;

import com.myce.common.dto.ExpoAdminBusinessProfileRequestDto;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;

public interface ExpoAdminBusinessProfileService {
    ExpoAdminBusinessProfileResponseDto getMyBusinessProfile(Long expoId, Long memberId);
    ExpoAdminBusinessProfileResponseDto updateMyBusinessProfile(Long expoId, Long memberId, ExpoAdminBusinessProfileRequestDto dto);
}