package com.myce.member.service;

import com.myce.member.dto.expo.ExpoAdminCodeResponse;
import com.myce.member.dto.expo.ExpoPaymentDetailResponse;
import com.myce.member.dto.expo.ExpoRefundReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementReceiptResponse;
import com.myce.member.dto.expo.ExpoSettlementRequest;
import com.myce.member.dto.expo.MemberExpoDetailResponse;
import com.myce.member.dto.expo.MemberExpoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberExpoService {
    
    Page<MemberExpoResponse> getMemberExpos(Long memberId, Pageable pageable);
    
    MemberExpoDetailResponse getMemberExpoDetail(Long memberId, Long expoId);
    
    void cancelExpo(Long memberId, Long expoId);
    
    ExpoPaymentDetailResponse getExpoPaymentDetail(Long memberId, Long expoId);
    
    List<ExpoAdminCodeResponse> getExpoAdminCodes(Long memberId, Long expoId);
    
    ExpoSettlementReceiptResponse getExpoSettlementReceipt(Long memberId, Long expoId);
    
    ExpoRefundReceiptResponse getExpoRefundReceipt(Long memberId, Long expoId);
    
    ExpoRefundReceiptResponse getExpoRefundHistory(Long memberId, Long expoId);
    
    void requestExpoSettlement(Long memberId, Long expoId, ExpoSettlementRequest request);
    
    void completeExpoPayment(Long memberId, Long expoId);
}