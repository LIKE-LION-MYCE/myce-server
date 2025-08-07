package com.myce.common.service.impl;

import com.myce.common.dto.ExpoAdminBusinessProfileRequestDto;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.service.ExpoAdminBusinessProfileService;
import com.myce.common.service.mapper.ExpoAdminBusinessProfileMapper;
import com.myce.expo.repository.ExpoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpoAdminBusinessProfileServiceImpl implements ExpoAdminBusinessProfileService {

    private final ExpoRepository expoRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpoAdminBusinessProfileMapper mapper;

    @Override//TODO:하위관리자
    public ExpoAdminBusinessProfileResponseDto getMyBusinessProfile(Long expoId, Long memberId) {
        BusinessProfile profile = validateMyBusinessProfileAccess(expoId, memberId);
        return mapper.toDto(profile);
    }

    @Override//TODO:하위관리자
    @Transactional
    public ExpoAdminBusinessProfileResponseDto updateMyBusinessProfile(Long expoId,
                                        Long memberId,
                                        ExpoAdminBusinessProfileRequestDto dto) {
        BusinessProfile profile = validateMyBusinessProfileAccess(expoId, memberId);

        profile.updateProfileInfo(
                dto.getLogoUrl(),
                dto.getCompanyName(),
                dto.getAddress(),
                dto.getCeoName(),
                dto.getContactEmail(),
                dto.getContactPhone(),
                dto.getBusinessRegistrationNumber()
        );

        return mapper.toDto(profile);
    }

    private BusinessProfile validateMyBusinessProfileAccess(Long expoId, Long memberId){
        if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        return businessProfileRepository.findByTargetIdAndTargetType(expoId,TargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));
    }
}