package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.BoothRegistrationRequest;
import com.myce.expo.dto.BoothRegistrationResponse;
import com.myce.expo.entity.Booth;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.BoothRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.BoothService;
import com.myce.expo.service.mapper.BoothMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoothServiceImpl implements BoothService {

    private final BoothRepository boothRepository;
    private final ExpoRepository expoRepository;
    private final BoothMapper boothMapper;

    // 부스 등록
    @Override
    public BoothRegistrationResponse saveBooth(Long expoId, BoothRegistrationRequest request, Long memberId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        // 박람회 관리자 권한 검증
        if (!expo.getMember().getId().equals(memberId)) {
            // TODO: 하위 관리자 권한 체크
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 프리미엄 부스 검증
        if (request.getIsPremium()) { // 프리미엄 부스가 참일 경우
            // 널 처리
            if (request.getDisplayRank() == null) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_REQUIRED);
            }
            // 순위는 3위까지 부여 가능
            if (request.getDisplayRank() < 1 || request.getDisplayRank() > 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_INVALID);
            }
            // 전체 부스 중 3개만 순위 부여 가능
            if (boothRepository.countByExpoIdAndIsPremiumTrue(expoId) >= 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_MAX_CAPACITY_REACHED);
            }
            // 중복으로 순위 값을 입력했을 경우
            if (boothRepository.existsByExpoIdAndIsPremiumTrueAndDisplayRank(expoId, request.getDisplayRank())) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_DUPLICATED);
            }
        }

        Booth booth = boothMapper.toEntity(request, expo);
        Booth savedBooth = boothRepository.save(booth);
        return boothMapper.toResponse(savedBooth);
    }
}
