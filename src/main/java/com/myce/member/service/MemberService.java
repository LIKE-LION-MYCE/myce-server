package com.myce.member.service;

import com.myce.member.dto.MemberInfoResponse;
import com.myce.member.dto.PaymentHistoryResponse;
import com.myce.member.dto.ReservedExpoResponse;

import java.util.List;

public interface MemberService {
    
    List<ReservedExpoResponse> getReservedExpos(Long memberId);
    
    MemberInfoResponse getMemberInfo(Long memberId);
    
    void withdrawMember(Long memberId);
    
    List<PaymentHistoryResponse> getPaymentHistory(Long memberId);
}