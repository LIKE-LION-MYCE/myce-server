package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.BoothRequest;
import com.myce.expo.dto.BoothResponse;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Booth;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.BoothRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.BoothService;
import com.myce.expo.service.mapper.BoothMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BoothServiceImpl implements BoothService {

    private final BoothRepository boothRepository;
    private final ExpoRepository expoRepository;
    private final BoothMapper boothMapper;
    private final AdminCodeRepository adminCodeRepository;
    private final AdminPermissionRepository adminPermissionRepository;

    @Override
    public BoothResponse saveBooth(Long expoId, BoothRequest request, LoginType loginType, Long principalId) {
        validateMyPermission(expoId, loginType, principalId);
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        validatePremiumBooth(request, expoId);

        Booth booth = boothMapper.toEntity(request, expo);
        Booth savedBooth = boothRepository.save(booth);
        return boothMapper.toResponse(savedBooth);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoothResponse> getMyBooths(Long expoId, LoginType loginType, Long principalId) {
        validateMyPermission(expoId, loginType, principalId);
        List<Booth> booths = boothRepository.findAllByExpoId(expoId);
        return booths.stream()
                .map(boothMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BoothResponse updateBooth(Long expoId, Long boothId, BoothRequest request, LoginType loginType, Long principalId) {
        validateMyPermission(expoId, loginType, principalId);
        Booth booth = getBoothAndValidate(expoId, boothId);

        validatePremiumBoothForUpdate(request, booth);

        booth.update(request);
        return boothMapper.toResponse(booth);
    }

    @Override
    public void deleteBooth(Long expoId, Long boothId, LoginType loginType, Long principalId) {
        validateMyPermission(expoId, loginType, principalId);
        Booth booth = getBoothAndValidate(expoId, boothId);
        boothRepository.delete(booth);
    }

    private void validateMyPermission(Long expoId, LoginType loginType, Long principalId) {
        switch (loginType) {
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, principalId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                AdminCode adminCode = adminCodeRepository.findById(principalId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.ADMIN_CODE_NOT_FOUND));
                if (!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsBoothInfoUpdateTrue(principalId, expoId)) {
                    throw new CustomException(CustomErrorCode.BOOTH_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }

    private Booth getBoothAndValidate(Long expoId, Long boothId) {
        Booth booth = boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BOOTH_NOT_FOUND));
        if (!booth.getExpo().getId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.BOOTH_NOT_BELONG_TO_EXPO);
        }
        return booth;
    }

    private void validatePremiumBooth(BoothRequest request, Long expoId) {
        if (request.getIsPremium()) {
            if (request.getDisplayRank() == null) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_REQUIRED);
            }
            if (request.getDisplayRank() < 1 || request.getDisplayRank() > 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_INVALID);
            }
            if (boothRepository.countByExpoIdAndIsPremiumTrue(expoId) >= 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_MAX_CAPACITY_REACHED);
            }
            if (boothRepository.existsByExpoIdAndIsPremiumTrueAndDisplayRank(expoId, request.getDisplayRank())) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_DUPLICATED);
            }
        }
    }

    private void validatePremiumBoothForUpdate(BoothRequest request, Booth booth) {
        if (request.getIsPremium()) {
            if (request.getDisplayRank() == null) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_REQUIRED);
            }
            if (request.getDisplayRank() < 1 || request.getDisplayRank() > 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_INVALID);
            }
            if (!booth.getIsPremium() && boothRepository.countByExpoIdAndIsPremiumTrue(booth.getExpo().getId()) >= 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_MAX_CAPACITY_REACHED);
            }
            boothRepository.findAllByExpoId(booth.getExpo().getId()).stream()
                    .filter(b -> !b.getId().equals(booth.getId()) && b.getIsPremium() && request.getDisplayRank().equals(b.getDisplayRank()))
                    .findAny()
                    .ifPresent(b -> {
                        throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_DUPLICATED);
                    });
        }
    }
}