package com.myce.system.service.fee.impl;

import com.myce.system.dto.fee.ExpoFeeRequest;
import com.myce.system.entity.ExpoFeeSetting;
import com.myce.system.repository.ExpoFeeSettingRepository;
import com.myce.system.service.fee.ExpoFeeService;
import com.myce.system.service.mapper.ExpoFeeMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoFeeServiceImpl implements ExpoFeeService {

    private final ExpoFeeMapper expoFeeMapper;
    private final ExpoFeeSettingRepository expoFeeSettingRepository;

    @Override
    @Transactional
    public void saveExpoFee(ExpoFeeRequest request) {
        if(request.getIsActive()) {
            updateAlreadyActiveSetting();
        }

        ExpoFeeSetting expoFeeSetting = expoFeeMapper.toExpoFeeSetting(request);
        expoFeeSettingRepository.save(expoFeeSetting);
    }

    private void updateAlreadyActiveSetting() {
        Optional<ExpoFeeSetting> expoFeeSettingOptional = expoFeeSettingRepository.findByIsActiveTrue();
        if(expoFeeSettingOptional.isEmpty()) return;

        ExpoFeeSetting expoFeeSetting = expoFeeSettingOptional.get();
        log.debug("Change expo fee active status to inactive. expoFeeId={}", expoFeeSetting.getId());
        expoFeeSetting.inactive();
    }
}
