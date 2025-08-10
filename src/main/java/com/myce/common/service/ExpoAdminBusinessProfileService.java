package com.myce.common.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.dto.ExpoAdminBusinessProfileRequestDto;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;

public interface ExpoAdminBusinessProfileService {
    ExpoAdminBusinessProfileResponseDto getMyBusinessProfile(Long expoId, Long memberId, LoginType loginType);
    ExpoAdminBusinessProfileResponseDto updateMyBusinessProfile(Long expoId,
                                                                Long memberId,
                                                                LoginType loginType,
                                                                ExpoAdminBusinessProfileRequestDto dto);
}