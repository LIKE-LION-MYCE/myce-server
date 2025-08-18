package com.myce.system.service.fee.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.fee.FeeActiveRequest;
import com.myce.system.dto.fee.PublicRefundPolicyListResponse;
import com.myce.system.dto.fee.RefundFeeListResponse;
import com.myce.system.dto.fee.RefundFeeRequest;
import com.myce.system.dto.fee.UpdateRefundFeeRequest;
import com.myce.system.entity.RefundFeeSetting;
import com.myce.system.repository.RefundFeeSettingRepository;
import com.myce.system.service.fee.RefundFeeService;
import com.myce.system.service.mapper.RefundFeeMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
    public RefundFeeListResponse getAllSettings(int page, String name) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 15, sort);
        Page<RefundFeeSetting> settings = refundFeeSettingRepository.findAll(pageable);
        return refundFeeMapper.toListResponse(settings);
    }

    @Override
    @Transactional
    public void updateRefundFee(long id, UpdateRefundFeeRequest request) {
        checkValidPeriod(request.getValidFrom(), request.getValidUntil());
        RefundFeeSetting setting = refundFeeSettingRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_REFUND_FEE_SETTING));
        setting.updateFeeSetting(request.getName(), request.getStandardDayCount(), request.getFeeRate(),
                request.getDescription(), request.getValidFrom(), request.getValidUntil());
    }

    @Override
    @Transactional
    public void updateRefundFeeActivation(long id, FeeActiveRequest request) {
        RefundFeeSetting setting = refundFeeSettingRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_REFUND_FEE_SETTING));
        if(request.getIsActive()) setting.active();
        else setting.inactive();
    }

    @Override
    public void saveRefundFee(RefundFeeRequest request) {
        checkValidPeriod(request.getValidFrom(), request.getValidUntil());
        RefundFeeSetting refundFeeSetting = refundFeeMapper.toRefundFeeSetting(request);
        refundFeeSettingRepository.save(refundFeeSetting);
    }

    private void checkValidPeriod(LocalDateTime validFrom, LocalDateTime validUntil) {
        if(refundFeeSettingRepository.existsByValidFromBeforeAndValidUntilAfter(validFrom, validUntil)) {
            throw new CustomException(CustomErrorCode.ALREADY_EXIST_FEE_IN_DATE);
        }
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