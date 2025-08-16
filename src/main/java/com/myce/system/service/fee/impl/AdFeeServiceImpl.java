package com.myce.system.service.fee.impl;

import com.myce.system.entity.AdPosition;
import com.myce.system.repository.AdPositionRepository;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.fee.AdFeeListResponse;
import com.myce.system.dto.fee.AdFeeRequest;
import com.myce.system.dto.fee.AdFeeResponse;
import com.myce.system.dto.fee.FeeActiveRequest;

import java.util.List;
import java.util.stream.Collectors;
import com.myce.system.entity.AdFeeSetting;
import com.myce.system.repository.AdFeeSettingRepository;
import com.myce.system.service.fee.AdFeeService;
import com.myce.system.service.mapper.AdFeeMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdFeeServiceImpl implements AdFeeService {

    private final AdPositionRepository adPositionRepository;
    private final AdFeeSettingRepository adFeeSettingRepository;
    private final AdFeeMapper adFeeMapper;

    @Override
    @Transactional
    public void saveAdFee(AdFeeRequest request) {
        Long adPositionId = request.getPositionId();
        AdPosition adPosition = adPositionRepository.findById(adPositionId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_POSITION_NOT_EXIST));

        if(request.getIsActive()) {
            updateAlreadyActiveSetting(adPositionId);
        }

        AdFeeSetting adFeeSetting = adFeeMapper.getAdFeeSetting(request, adPosition);
        adFeeSettingRepository.save(adFeeSetting);
    }

    @Override
    public AdFeeListResponse getAdFeeList(int page, Long positionId, String name) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, 10, sort);
        Page<AdFeeSetting> adFeeSettings;

        if(positionId != null && name != null)
            adFeeSettings = adFeeSettingRepository.findAllByAdPosition_IdAndNameContaining(positionId, name, pageable);
        else if(positionId != null)
            adFeeSettings = adFeeSettingRepository.findAllByAdPosition_Id(positionId, pageable);
        else if(name != null)
            adFeeSettings = adFeeSettingRepository.findAllByNameContains(name, pageable);
        else
            adFeeSettings= adFeeSettingRepository.findAll(pageable);

        return adFeeMapper.toListResponse(adFeeSettings);
    }

    @Override
    @Transactional
    public void updateAdFeeActivation(Long targetId, FeeActiveRequest request) {
        AdFeeSetting adFeeSetting = adFeeSettingRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_AD_FEE_SETTING));

        boolean isActive = request.getIsActive();
        if(isActive == adFeeSetting.getIsActive())
            throw new CustomException(CustomErrorCode.ALREADY_SET_ACTIVATION);


        if(isActive) {
            updateAlreadyActiveSetting(adFeeSetting.getAdPosition().getId());
            adFeeSetting.active();
        } else adFeeSetting.inactive();
    }

    @Override
    public List<AdFeeResponse> getActiveAdFees() {
        // 활성화된 광고 위치들을 가져옴
        List<AdPosition> activePositions = adPositionRepository.findAllByIsActiveTrue();
        
        return activePositions.stream()
                .map(position -> {
                    // 각 위치별로 활성화된 요금제를 찾음
                    Optional<AdFeeSetting> activeFeeOpt = 
                            adFeeSettingRepository.findByAdPositionIdAndIsActiveTrue(position.getId());
                    
                    if (activeFeeOpt.isPresent()) {
                        return adFeeMapper.toAdFeeResponse(activeFeeOpt.get());
                    }
                    return null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    private void updateAlreadyActiveSetting(Long adPositionId) {
        Optional<AdFeeSetting> optionalAdFeeSetting =
                adFeeSettingRepository.findByAdPositionIdAndIsActiveTrue(adPositionId);
        if(optionalAdFeeSetting.isEmpty()) return;

        AdFeeSetting adFeeSetting = optionalAdFeeSetting.get();
        log.debug("Change ad fee active status to inactive. adFeeId={}", adFeeSetting.getId());
        adFeeSetting.inactive();
    }
}