package com.myce.common.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.dto.ExpoAdminBusinessProfileRequestDto;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.service.ExpoAdminBusinessProfileService;
import com.myce.common.service.mapper.ExpoAdminBusinessProfileMapper;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpoAdminBusinessProfileServiceImpl implements ExpoAdminBusinessProfileService {

    private final ExpoRepository expoRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final ExpoAdminBusinessProfileMapper mapper;

    @Override
    public ExpoAdminBusinessProfileResponseDto getMyBusinessProfile(Long expoId, Long memberId, LoginType loginType) {
        validateMyAccess(expoId, memberId, loginType);
        BusinessProfile profile = getMyBusinessProfile(expoId);

        return mapper.toDto(profile);
    }

    @Override
    @Transactional
    public ExpoAdminBusinessProfileResponseDto updateMyBusinessProfile(Long expoId,
                                        Long memberId,
                                        LoginType loginType,
                                        ExpoAdminBusinessProfileRequestDto dto) {
        validateMyAccess(expoId, memberId, loginType);
        BusinessProfile profile = getMyBusinessProfile(expoId);

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

    private BusinessProfile getMyBusinessProfile(Long expoId){
        return businessProfileRepository.findByTargetIdAndTargetType(expoId,TargetType.EXPO)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));
    }

    private void validateMyAccess(Long expoId, Long memberId, LoginType loginType) {
        if(memberId == null || loginType == null){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch(loginType){
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                if(!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsOperationsConfigUpdateTrue(memberId, expoId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}