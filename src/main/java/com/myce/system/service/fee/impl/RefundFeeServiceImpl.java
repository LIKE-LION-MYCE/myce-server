package com.myce.system.service.fee.impl;

import com.myce.system.dto.fee.RefundFeeListResponse;
import com.myce.system.entity.RefundFeeSetting;
import com.myce.system.repository.RefundFeeSettingRepository;
import com.myce.system.service.fee.RefundFeeService;
import com.myce.system.service.mapper.RefundFeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundFeeServiceImpl implements RefundFeeService {

    private final RefundFeeMapper refundFeeMapper;
    private final RefundFeeSettingRepository refundFeeSettingRepository;

    @Override
    public RefundFeeListResponse getAllSettings(int page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 15, sort);
        Page<RefundFeeSetting> settings = refundFeeSettingRepository.findAll(pageable);
        return refundFeeMapper.toListResponse(settings);
    }
}