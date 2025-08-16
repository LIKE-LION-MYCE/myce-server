package com.myce.member.service;

import com.myce.member.dto.*;
import com.myce.member.dto.expo.FavoriteExpoResponse;
import com.myce.member.dto.expo.ReservedExpoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberMyPageService {
    
    Page<ReservedExpoResponse> getReservedExpos(Long memberId, Pageable pageable);

    List<FavoriteExpoResponse> getFavoriteExpos(Long memberId);

    MemberInfoResponse getMemberInfo(Long memberId);

    void updateMemberInfo(Long memberId, MemberInfoUpdateRequest request);
    
    Page<PaymentHistoryResponse> getPaymentHistory(Long memberId, Pageable pageable);
    
    MemberSettingResponse getMemberSetting(Long memberId);
    
    void updateMemberSetting(Long memberId, MemberSettingUpdateRequest request);
}