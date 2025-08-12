package com.myce.system.service.fee.impl;

import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.repository.AdPositionRepository;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.fee.AdFeeRequest;
import com.myce.system.entity.AdFeeSetting;
import com.myce.system.repository.AdFeeSettingRepository;
import com.myce.system.service.fee.AdFeeService;
import com.myce.system.service.mapper.AdFeeMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private void updateAlreadyActiveSetting(Long adPositionId) {
        Optional<AdFeeSetting> optionalAdFeeSetting =
                adFeeSettingRepository.findByAdPositionIdAndIsActiveTrue(adPositionId);
        if(optionalAdFeeSetting.isEmpty()) return;

        AdFeeSetting adFeeSetting = optionalAdFeeSetting.get();
        log.debug("Change ad fee active status to inactive. adFeeId={}", adFeeSetting.getId());
        adFeeSetting.inactive();
        adFeeSettingRepository.save(adFeeSetting);
        adFeeSettingRepository.flush();
    }
}