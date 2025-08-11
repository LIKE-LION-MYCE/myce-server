package com.myce.member.service;

import com.myce.member.dto.*;

import java.util.List;

public interface MemberAdService {
    
    List<MemberAdvertisementResponse> getMemberAdvertisements(Long memberId);
    
    AdvertisementDetailResponse getAdvertisementDetail(Long memberId, Long advertisementId);
    
    void cancelAdvertisement(Long memberId, Long advertisementId);
    
    AdvertisementPaymentDetailResponse getAdvertisementPaymentDetail(Long memberId, Long advertisementId);
    
    AdvertisementRefundReceiptResponse getAdvertisementRefundReceipt(Long memberId, Long advertisementId);
}