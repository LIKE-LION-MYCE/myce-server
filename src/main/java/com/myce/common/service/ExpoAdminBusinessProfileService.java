package com.myce.common.service;

import com.myce.common.dto.ExpoAdminBusinessProfileRequestDto;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;

public interface ExpoAdminBusinessProfileService {
    ExpoAdminBusinessProfileResponseDto getMyBusinessProfile(Long memberId);
    void updateMyBusinessProfile(Long memberId, Long profileId, ExpoAdminBusinessProfileRequestDto dto);
}