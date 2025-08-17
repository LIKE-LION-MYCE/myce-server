package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
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
    private final ExpoAdminAccessValidate expoAdminAccessValidate;

    @Override
    public BoothResponse saveBooth(Long expoId, BoothRequest request, LoginType loginType, Long principalId) {
        expoAdminAccessValidate.ensureEditable(expoId, principalId, loginType, ExpoAdminPermission.BOOTH_INFO_UPDATE);
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
        expoAdminAccessValidate.ensureViewable(expoId, principalId, loginType, ExpoAdminPermission.BOOTH_INFO_UPDATE);
        List<Booth> booths = boothRepository.findAllByExpoId(expoId);
        return booths.stream()
                .map(boothMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BoothResponse updateBooth(Long expoId, Long boothId, BoothRequest request, LoginType loginType, Long principalId) {
        expoAdminAccessValidate.ensureEditable(expoId, principalId, loginType, ExpoAdminPermission.BOOTH_INFO_UPDATE);
        Booth booth = getBoothAndValidate(expoId, boothId);

        validatePremiumBoothForUpdate(request, booth);

        booth.update(request);
        return boothMapper.toResponse(booth);
    }

    @Override
    public void deleteBooth(Long expoId, Long boothId, LoginType loginType, Long principalId) {
        expoAdminAccessValidate.ensureEditable(expoId, principalId, loginType, ExpoAdminPermission.BOOTH_INFO_UPDATE);
        Booth booth = getBoothAndValidate(expoId, boothId);
        boothRepository.delete(booth);
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
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        
        // 박람회가 프리미엄 부스 서비스를 신청한 경우에만 검증
        if (expo.getIsPremium()) {
            // 부스를 프리미엄으로 설정하려는 경우
            if (request.getIsPremium() != null && request.getIsPremium()) {
                // 이미 3개의 프리미엄 부스가 있는지 확인
                long premiumBoothCount = boothRepository.countByExpoIdAndIsPremiumTrue(expoId);
                if (premiumBoothCount >= 3) {
                    throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_MAX_CAPACITY_REACHED);
                }
                
                // 프리미엄 부스는 반드시 순위(1~3)가 있어야 함
                if (request.getDisplayRank() == null || request.getDisplayRank() <= 0 || request.getDisplayRank() > 3) {
                    throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_REQUIRED);
                }
                
                // 해당 순위가 이미 사용중인지 확인
                if (boothRepository.existsByExpoIdAndIsPremiumTrueAndDisplayRank(expoId, request.getDisplayRank())) {
                    throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_DUPLICATED);
                }
            }
        }
    }

    private void validatePremiumBoothForUpdate(BoothRequest request, Booth booth) {
        Expo expo = booth.getExpo();
        
        // 박람회가 프리미엄 부스 서비스를 신청한 경우에만 검증
        if (expo.getIsPremium()) {
            // 부스를 프리미엄으로 설정하려는 경우
            if (request.getIsPremium() != null && request.getIsPremium()) {
                // 기존에 프리미엄이 아니었다면 3개 제한 확인
                if (!booth.getIsPremium()) {
                    long premiumBoothCount = boothRepository.countByExpoIdAndIsPremiumTrue(expo.getId());
                    if (premiumBoothCount >= 3) {
                        throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_MAX_CAPACITY_REACHED);
                    }
                }
                
                // 프리미엄 부스는 반드시 순위(1~3)가 있어야 함
                if (request.getDisplayRank() == null || request.getDisplayRank() <= 0 || request.getDisplayRank() > 3) {
                    throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_REQUIRED);
                }
                
                // 다른 프리미엄 부스가 이미 같은 순위를 사용중인지 확인 (자기 자신 제외)
                boolean rankExists = boothRepository.findAllByExpoId(expo.getId()).stream()
                        .anyMatch(b -> !b.getId().equals(booth.getId()) && 
                                      b.getIsPremium() && 
                                      request.getDisplayRank().equals(b.getDisplayRank()));
                if (rankExists) {
                    throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_DUPLICATED);
                }
            }
        }
    }
}