package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.expo.dto.ExpoAdminPermissionResponse;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoUpdateRequest;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Category;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.CategoryRepository;
import com.myce.expo.repository.ExpoCategoryRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.MyExpoService;
import com.myce.expo.service.mapper.ExpoAdminPermissionMapper;
import com.myce.expo.service.mapper.MyExpoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MyExpoServiceImpl implements MyExpoService {

    private final ExpoRepository expoRepository;
    private final ExpoCategoryRepository expoCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final MyExpoMapper expoMapper;
    private final ExpoAdminPermissionMapper expoAdminPermissionMapper;
    private final ExpoAdminAccessValidate expoAdminAccessValidate;

    @Override
    public ExpoAdminPermissionResponse getExpoAdminPermission(Long memberId, LoginType loginType) {
        if (loginType == null || memberId == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch (loginType) {
            case MEMBER -> {
                List<Long> expoIds = expoRepository.findIdsByMemberIdAndStatusIn(memberId, ExpoStatus.ADMIN_VIEWABLE_STATUSES);
                return expoAdminPermissionMapper.toDto(expoIds, null);
            }
            case ADMIN_CODE -> {
                AdminCode adminCode = adminCodeRepository.findWithAdminPermissionById(memberId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.ADMIN_CODE_NOT_FOUND));

                return expoAdminPermissionMapper.toDto(List.of(adminCode.getExpoId()), adminCode.getAdminPermission());
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MyExpoDetailResponse getMyExpoDetail(Long expoId, LoginType loginType, Long principalId) {
        expoAdminAccessValidate.ensureViewable(expoId, principalId, loginType, ExpoAdminPermission.EXPO_DETAIL_UPDATE);
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        List<ExpoCategory> expoCategories = expoCategoryRepository.findByExpoId(expo.getId());
        return expoMapper.toMyExpoDetailResponse(expo, expoCategories);
    }

    @Override
    public MyExpoDetailResponse updateMyExpoDetail(Long expoId, MyExpoUpdateRequest updateRequest, LoginType loginType, Long principalId) {
        // 박람회 수정은 특별히 PENDING_PUBLISH 상태에서만 허용
        validateExpoUpdatePermission(expoId, principalId, loginType);
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        expo.updateFromDto(updateRequest);
        updateExpoCategories(expo, updateRequest.getCategoryIds());

        List<ExpoCategory> expoCategories = expoCategoryRepository.findByExpoId(expo.getId());
        return expoMapper.toMyExpoDetailResponse(expo, expoCategories);
    }

    private void validateExpoUpdatePermission(Long expoId, Long principalId, LoginType loginType) {
        // 기본 권한 검증 (EXPO_DETAIL_UPDATE 권한 필요)
        expoAdminAccessValidate.ensureViewable(expoId, principalId, loginType, ExpoAdminPermission.EXPO_DETAIL_UPDATE);
        
        // 박람회 수정은 특별히 PENDING_PUBLISH 상태에서만 허용
        ExpoStatus status = expoRepository.findStatusById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        
        if (status != ExpoStatus.PENDING_PUBLISH) {
            throw new CustomException(CustomErrorCode.EXPO_EDIT_DENIED);
        }
    }

    private void updateExpoCategories(Expo expo, List<Long> categoryIds) {
        expoCategoryRepository.deleteAllByExpo(expo);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                throw new CustomException(CustomErrorCode.CATEGORY_NOT_EXIST);
            }
            List<ExpoCategory> newExpoCategories = categories.stream()
                    .map(category -> ExpoCategory.builder().expo(expo).category(category).build())
                    .collect(Collectors.toList());
            expoCategoryRepository.saveAll(newExpoCategories);
        }
    }
}