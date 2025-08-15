package com.myce.system.service.fee.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.fee.ExpoFeeListResponse;
import com.myce.system.dto.fee.ExpoFeeRequest;
import com.myce.system.dto.fee.ExpoFeeResponse;
import com.myce.system.dto.fee.FeeActiveRequest;
import com.myce.system.entity.ExpoFeeSetting;
import com.myce.system.repository.ExpoFeeSettingRepository;
import com.myce.system.service.fee.ExpoFeeService;
import com.myce.system.service.mapper.ExpoFeeMapper;
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

    @Override
    public ExpoFeeListResponse getExpoFeeList(int page, String name) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 10, sort);
        Page<ExpoFeeSetting> expoFeeSettings;
        if(name != null) expoFeeSettings = expoFeeSettingRepository.findAllByNameContaining(name, pageable);
        else expoFeeSettings = expoFeeSettingRepository.findAll(pageable);

        return expoFeeMapper.toListResponse(expoFeeSettings);
    }

    @Override
    @Transactional
    public void updateExpoFeeActivation(Long targetId, FeeActiveRequest request) {
        ExpoFeeSetting expoFeeSetting = expoFeeSettingRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_EXPO_FEE_SETTING));

        boolean isActive = request.getIsActive();
        if(isActive) {
            updateAlreadyActiveSetting();
            expoFeeSetting.active();
        } else expoFeeSetting.inactive();
    }

    @Override
    public ExpoFeeResponse getActiveExpoFee() {
        ExpoFeeSetting expoFeeSetting = expoFeeSettingRepository.findByIsActiveTrue()
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_EXPO_FEE_SETTING));
        
        return expoFeeMapper.toResponse(expoFeeSetting);
    }

    private void updateAlreadyActiveSetting() {
        Optional<ExpoFeeSetting> expoFeeSettingOptional = expoFeeSettingRepository.findByIsActiveTrue();
        if(expoFeeSettingOptional.isEmpty()) return;

        ExpoFeeSetting expoFeeSetting = expoFeeSettingOptional.get();
        log.debug("Change expo fee active status to inactive. expoFeeId={}", expoFeeSetting.getId());
        expoFeeSetting.inactive();
    }
}
