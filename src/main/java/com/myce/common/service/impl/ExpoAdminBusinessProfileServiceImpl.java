package com.myce.common.service.impl;

import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.service.ExpoAdminBusinessProfileService;
import com.myce.common.service.mapper.ExpoAdminBusinessProfileMapper;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpoAdminBusinessProfileServiceImpl implements ExpoAdminBusinessProfileService {

    private final ExpoRepository expoRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpoAdminBusinessProfileMapper mapper;

    @Override//TODO:하위관리자
    public ExpoAdminBusinessProfileResponseDto getExpoAdminBusinessProfile(Long memberId) {
        Expo expo = getActiveExpo(memberId);

        BusinessProfile profile = businessProfileRepository.findByTargetIdAndTargetType(expo.getId(), TargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return mapper.toDto(profile);
    }

    private Expo getActiveExpo(Long memberId) {
        return expoRepository.findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(memberId, ExpoStatus.ACTIVE_STATUSES)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
    }
}
