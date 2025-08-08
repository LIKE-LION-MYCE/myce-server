package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.ExpoAdminManagerResponse;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.ExpoAdminManagerService;
import com.myce.expo.service.mapper.ExpoAdminMangerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpoAdminManagerServiceImpl implements ExpoAdminManagerService {

    private final AdminCodeRepository adminCodeRepository;
    private final ExpoRepository expoRepository;
    private final ExpoAdminMangerMapper mapper;

    @Override//TODO:하위관리자
    public List<ExpoAdminManagerResponse> getMyExpoManagers(Long expoId, Long memberId) {

        if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        List<AdminCode> adminCodes = adminCodeRepository.findAllWithAdminPermissionByExpoId(expoId);

        return adminCodes.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
