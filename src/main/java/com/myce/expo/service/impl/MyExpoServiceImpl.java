package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.expo.dto.ExpoAdminPermissionResponse;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoDescriptionUpdateRequest;
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
    public MyExpoDetailResponse updateMyExpoDescription(Long expoId, MyExpoDescriptionUpdateRequest updateRequest, LoginType loginType, Long principalId) {
        // 권한 검증 (EXPO_DETAIL_UPDATE 권한 필요)
        expoAdminAccessValidate.ensureViewable(expoId, principalId, loginType, ExpoAdminPermission.EXPO_DETAIL_UPDATE);
        
        // 박람회 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        // 설명만 업데이트 (상태 제한 없음 - 프론트엔드에서 제어)
        expo.updateDescription(updateRequest.getDescription());

        List<ExpoCategory> expoCategories = expoCategoryRepository.findByExpoId(expo.getId());
        return expoMapper.toMyExpoDetailResponse(expo, expoCategories);
    }
}