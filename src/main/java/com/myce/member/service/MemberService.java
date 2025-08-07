package com.myce.member.service;

import com.myce.member.dto.*;

import java.util.List;

public interface MemberService {
    
    List<ReservedExpoResponse> getReservedExpos(Long memberId);

    List<FavoriteExpoResponse> getFavoriteExpos(Long memberId);

    MemberInfoResponse getMemberInfo(Long memberId);
    
    void withdrawMember(Long memberId);
    
    List<PaymentHistoryResponse> getPaymentHistory(Long memberId);
    
    MemberSettingResponse getMemberSetting(Long memberId);
    
    void updateMemberSetting(Long memberId, MemberSettingUpdateRequest request);
    
    List<MemberAdvertisementResponse> getMemberAdvertisements(Long memberId);
}