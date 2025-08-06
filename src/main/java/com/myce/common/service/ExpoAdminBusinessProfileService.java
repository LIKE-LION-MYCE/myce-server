package com.myce.common.service;

import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;

public interface ExpoAdminBusinessProfileService {
    ExpoAdminBusinessProfileResponseDto getExpoAdminBusinessProfile(Long memberId);
}