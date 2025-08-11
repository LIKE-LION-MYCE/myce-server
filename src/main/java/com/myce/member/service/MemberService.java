package com.myce.member.service;

import com.myce.member.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {
    
    List<ReservedExpoResponse> getReservedExpos(Long memberId);

    List<FavoriteExpoResponse> getFavoriteExpos(Long memberId);

    MemberInfoResponse getMemberInfo(Long memberId);

    void updateMemberInfo(Long memberId, MemberInfoUpdateRequest request);
    
    void withdrawMember(Long memberId);
    
    Page<PaymentHistoryResponse> getPaymentHistory(Long memberId, Pageable pageable);
    
    MemberSettingResponse getMemberSetting(Long memberId);
    
    void updateMemberSetting(Long memberId, MemberSettingUpdateRequest request);
    
    List<MemberAdvertisementResponse> getMemberAdvertisements(Long memberId);
    
    AdvertisementDetailResponse getAdvertisementDetail(Long memberId, Long advertisementId);
    
    void cancelAdvertisement(Long memberId, Long advertisementId);
    
    AdvertisementPaymentDetailResponse getAdvertisementPaymentDetail(Long memberId, Long advertisementId);
    
    AdvertisementRefundReceiptResponse getAdvertisementRefundReceipt(Long memberId, Long advertisementId);
    
    List<MemberExpoResponse> getMemberExpos(Long memberId);
    
    MemberExpoDetailResponse getMemberExpoDetail(Long memberId, Long expoId);
    
    void cancelExpo(Long memberId, Long expoId);
    
    ExpoPaymentDetailResponse getExpoPaymentDetail(Long memberId, Long expoId);
    
    List<ExpoAdminCodeResponse> getExpoAdminCodes(Long memberId, Long expoId);
    
    ExpoSettlementReceiptResponse getExpoSettlementReceipt(Long memberId, Long expoId);
    
    ExpoRefundReceiptResponse getExpoRefundReceipt(Long memberId, Long expoId);
}