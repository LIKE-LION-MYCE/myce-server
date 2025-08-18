package com.myce.system.service.fee.impl;

import com.myce.system.dto.fee.PublicRefundPolicyListResponse;
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

import java.time.LocalDateTime;
import java.util.List;

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
    
    @Override
    public PublicRefundPolicyListResponse getActivePublicRefundPolicy() {
        // 현재 활성화된 환불 정책만 조회 (standardDayCount 기준으로 정렬)
        LocalDateTime now = LocalDateTime.now();
        List<RefundFeeSetting> activeSettings = refundFeeSettingRepository.findActiveRefundSettings(now);
        
        // 페이지네이션 없이 모든 활성 설정을 가져와서 변환
        RefundFeeListResponse refundFeeList = refundFeeMapper.toListResponseFromList(activeSettings);
        
        return PublicRefundPolicyListResponse.from(refundFeeList);
    }
}