package com.myce.refund.service;

import com.myce.refund.dto.RefundRequestDto;

public interface RefundRequestService {
    
    // 환불 신청
    void createRefundRequest(Long memberId, Long expoId, RefundRequestDto requestDto);
    
}