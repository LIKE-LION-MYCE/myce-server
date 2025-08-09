package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.ExpoAdminPermissionResponse;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoUpdateRequest;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Category;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.CategoryRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.ExpoCategoryRepository;
import com.myce.expo.service.MyExpoService;
import com.myce.expo.service.mapper.ExpoAdminPermissionMapper;
import com.myce.expo.service.mapper.MyExpoMapper;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyExpoServiceImpl implements MyExpoService {

    private final ExpoRepository expoRepository;
    private final ExpoCategoryRepository expoCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final MyExpoMapper expoMapper;
    private final ExpoAdminPermissionMapper expoAdminPermissionMapper;

    @Override
    public ExpoAdminPermissionResponse getExpoAdminPermission(Long memberId, LoginType loginType) {
        if(loginType == null || memberId == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch (loginType) {
            case MEMBER -> {
                List<Expo> expos = expoRepository.findByMemberIdAndStatusIn(memberId, ExpoStatus.ACTIVE_STATUSES);
                List<Long> expoIds = expos.stream().map(Expo::getId).toList();

                return expoAdminPermissionMapper.toDto(expoIds, null);
            }

            case ADMIN_CODE -> {
                AdminCode adminCode = adminCodeRepository.findAllWithAdminPermissionById(memberId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.ADMIN_CODE_NOT_FOUND));

                return expoAdminPermissionMapper.toDto(List.of(adminCode.getExpoId()), adminCode.getAdminPermission());
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }

    // 내 박람회 상세조회
    @Override
    @Transactional(readOnly = true)
    public MyExpoDetailResponse getMyExpoDetail(Long expoId, Long memberId) {
        Expo expo = findAndValidateExpo(expoId, memberId);
        List<ExpoCategory> expoCategories = expoCategoryRepository.findByExpoId(expo.getId());
        return expoMapper.toMyExpoDetailResponse(expo, expoCategories);
    }

    // 내 박람회 수정
    @Override
    @Transactional
    public MyExpoDetailResponse updateMyExpoDetail(Long expoId, Long memberId, MyExpoUpdateRequest updateRequest) {
        // 박람회 조회 및 회원이 박람회 관리자인지 검증
        Expo expo = findAndValidateExpo(expoId, memberId);

        // TODO: 박람회 관리자 수정 권한 검증

        // 엔티티 업데이트 (더티체킹)
        expo.updateFromDto(updateRequest);

        // 카테고리 업데이트 로직
        updateExpoCategories(expo, updateRequest.getCategoryIds());

        // 업데이트된 엔티티를 DTO로 변환하여 반환
        List<ExpoCategory> expoCategories = expoCategoryRepository.findByExpoId(expo.getId());
        return expoMapper.toMyExpoDetailResponse(expo, expoCategories);
    }

    /// 유틸 메소드
    // 박람회 검증 및 박람회 관리자 검증 로직
    private Expo findAndValidateExpo(Long expoId, Long memberId) {
        // 박람회 조회
        Expo expo = expoRepository.findById(expoId).orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        // 로그인한 회원과 박람회 신청한 회원이 일치하는지 검증
        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 회원의 롤(Role)이 EXPO_ADMIN인지 확인합니다.
        if (member.getRole() != Role.EXPO_ADMIN) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        return expo;
    }

    // 박람회 카테고리를 업데이트하는 로직
    private void updateExpoCategories(Expo expo, List<Long> categoryIds) {
        // 엑스포 관련 카테고리 초기화
        expoCategoryRepository.deleteAllByExpo(expo);

        // 카테고리 ID로 카테고리 목록 가져오기
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new CustomException(CustomErrorCode.CATEGORY_NOT_EXIST);
        }

        // 업데이트
        List<ExpoCategory> newExpoCategories = categories.stream()
                .map(category -> ExpoCategory.builder()
                        .expo(expo)
                        .category(category)
                        .build())
                .collect(Collectors.toList());

        expoCategoryRepository.saveAll(newExpoCategories);
    }

    // TODO: 박람회 하위 관리자 수정 권한 검증
    private void validateUpdatePermission(Long memberId, Long expoId) {
        // TODO: AdminCode 무엇으로 조회?

        // AdminCode를 통해 AdminPermission 엔티티를 조회

        // isExpoDetailUpdate 권한이 true인지 확인

    }

}
