package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.ExpoCategoryRepository;
import com.myce.expo.service.MyExpoService;
import com.myce.expo.service.mapper.MyExpoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyExpoServiceImpl implements MyExpoService {

    private final ExpoRepository expoRepository;
    private final ExpoCategoryRepository expoCategoryRepository;
    private final MyExpoMapper expoMapper;

    @Override
    @Transactional(readOnly = true)
    public MyExpoDetailResponse getMyExpoDetail(Long expoId, Long memberId) {
        Expo expo = findAndValidateExpo(expoId, memberId);
        List<ExpoCategory> expoCategories = expoCategoryRepository.findByExpoId(expo.getId());
        return expoMapper.toMyExpoDetailResponse(expo, expoCategories);
    }

    /// 유틸 메소드
    // 박람회 조회 및 박람회 관리자 검증 로직
    private Expo findAndValidateExpo(Long expoId, Long memberId) {
        // 박람회 조회
        Expo expo = expoRepository.findById(expoId).orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        // 로그인한 회원과 박람회 신청한 회원이 일치하는지 검증
        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        //엔티티 반환
        return expo;
    }

}
