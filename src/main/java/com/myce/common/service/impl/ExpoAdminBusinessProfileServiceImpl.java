package com.myce.common.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.dto.ExpoAdminBusinessProfileRequestDto;
import com.myce.common.dto.ExpoAdminBusinessProfileResponseDto;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
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

    private final ExpoAdminAccessValidate expoAdminAccessValidate;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpoAdminBusinessProfileMapper mapper;

    @Override
    public ExpoAdminBusinessProfileResponseDto getMyBusinessProfile(Long expoId, Long memberId, LoginType loginType) {
        expoAdminAccessValidate.ensureViewable(expoId, memberId, loginType, ExpoAdminPermission.OPERATIONS_CONFIG_UPDATE);
        BusinessProfile profile = getMyBusinessProfile(expoId);

        return mapper.toDto(profile);
    }

    @Override
    @Transactional
    public ExpoAdminBusinessProfileResponseDto updateMyBusinessProfile(Long expoId,
                                        Long memberId,
                                        LoginType loginType,
                                        ExpoAdminBusinessProfileRequestDto dto) {
        expoAdminAccessValidate.ensureEditable(expoId, memberId, loginType, ExpoAdminPermission.OPERATIONS_CONFIG_UPDATE);
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
}