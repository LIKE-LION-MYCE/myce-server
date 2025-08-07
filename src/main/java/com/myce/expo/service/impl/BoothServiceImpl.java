package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.BoothRequest;
import com.myce.expo.dto.BoothResponse;
import com.myce.expo.entity.Booth;
import com.myce.expo.entity.Expo;
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

    // 부스 등록
    @Override
    public BoothResponse saveBooth(Long expoId, BoothRequest request, Long memberId) {
        Expo expo = findExpoAndVerifyOwner(expoId, memberId);

        // 프리미엄 부스 검증
        validatePremiumBooth(request, expoId);

        Booth booth = boothMapper.toEntity(request, expo);
        Booth savedBooth = boothRepository.save(booth);
        return boothMapper.toResponse(savedBooth);
    }

    // 해당 박람회의 부스 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<BoothResponse> getMyBooths(Long expoId, Long memberId) {
        findExpoAndVerifyOwner(expoId, memberId);

        List<Booth> booths = boothRepository.findAllByExpoId(expoId);

        return booths.stream()
                .map(boothMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 부스 수정
    @Override
    @Transactional
    public BoothResponse updateBooth(Long expoId, Long boothId, BoothRequest request, Long memberId) {
        findExpoAndVerifyOwner(expoId, memberId);

        // 부스 존재 여부 확인
        Booth booth = boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BOOTH_NOT_FOUND));

        // 부스가 해당 박람회에 속해 있는지 확인
        if (!booth.getExpo().getId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.BOOTH_NOT_BELONG_TO_EXPO);
        }

        // 프리미엄 부스 정책 검증
        validatePremiumBoothForUpdate(request, booth);

        // 부스 정보 업데이트
        booth.update(request);

        return boothMapper.toResponse(booth);
    }

    // 부스 삭제
    @Override
    @Transactional
    public void deleteBooth(Long expoId, Long boothId, Long memberId) {
        findExpoAndVerifyOwner(expoId, memberId);

        // 부스 존재 여부 확인
        Booth booth = boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BOOTH_NOT_FOUND));

        // 부스가 해당 박람회에 속해 있는지 확인
        if (!booth.getExpo().getId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.BOOTH_NOT_BELONG_TO_EXPO);
        }

        boothRepository.delete(booth);
    }

    ///  유틸 메소드

    // 박람회 존재 여부 및 소유권 확인
    private Expo findExpoAndVerifyOwner(Long expoId, Long memberId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        // 박람회 관리자 권한 검증
        if (!expo.getMember().getId().equals(memberId)) {
            // TODO: 하위 관리자 권한 체크
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        return expo;
    }

    // 등록시 프리미엄 부스 검증
    private void validatePremiumBooth(BoothRequest request, Long expoId) {
        // 프리미엄 부스인 경우
        if (request.getIsPremium()) {
            // 순위가 널인 경우 예외 발생
            if (request.getDisplayRank() == null) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_REQUIRED);
            }
            // 순위값이 1~3이 아닌 경우 예외 발생
            if (request.getDisplayRank() < 1 || request.getDisplayRank() > 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_INVALID);
            }
            // 총 프리미엄 부스 수가 3개 초과일 경우 예외 발생
            if (boothRepository.countByExpoIdAndIsPremiumTrue(expoId) >= 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_MAX_CAPACITY_REACHED);
            }
            // 중복된 순위값일 경우 예외 발생
            if (boothRepository.existsByExpoIdAndIsPremiumTrueAndDisplayRank(expoId, request.getDisplayRank())) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_DUPLICATED);
            }
        }
    }

    // 수정시 프리미엄 부스 검증
    private void validatePremiumBoothForUpdate(BoothRequest request, Booth booth) {
        if (request.getIsPremium()) {
            if (request.getDisplayRank() == null) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_REQUIRED);
            }
            if (request.getDisplayRank() < 1 || request.getDisplayRank() > 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_INVALID);
            }
            // 현재 부스가 프리미엄이 아니었다가 프리미엄으로 변경되는 경우, 개수 제한 확인
            if (!booth.getIsPremium() && boothRepository.countByExpoIdAndIsPremiumTrue(booth.getExpo().getId()) >= 3) {
                throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_MAX_CAPACITY_REACHED);
            }
            // 다른 부스가 이미 해당 순위를 사용하고 있는지 확인
            boothRepository.findAllByExpoId(booth.getExpo().getId()).stream()
                    // 해당 부스의 다른 id를 가진, 프리미엄 부스 가운데, 순위가 같은 부스가 존재한다면 예외 발생
                    .filter(b -> !b.getId().equals(booth.getId()) && b.getIsPremium() && request.getDisplayRank().equals(b.getDisplayRank()))
                    .findAny()
                    .ifPresent(b -> {
                        throw new CustomException(CustomErrorCode.BOOTH_PREMIUM_RANK_DUPLICATED);
                    });
        }
    }
}
