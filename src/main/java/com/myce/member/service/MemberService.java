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
    
    AdvertisementDetailResponse getAdvertisementDetail(Long memberId, Long advertisementId);
    
    void cancelAdvertisement(Long memberId, Long advertisementId);
    
    AdvertisementPaymentDetailResponse getAdvertisementPaymentDetail(Long memberId, Long advertisementId);
    
    AdvertisementRefundReceiptResponse getAdvertisementRefundReceipt(Long memberId, Long advertisementId);
    
    List<MemberExpoResponse> getMemberExpos(Long memberId);
    
    MemberExpoDetailResponse getMemberExpoDetail(Long memberId, Long expoId);
}