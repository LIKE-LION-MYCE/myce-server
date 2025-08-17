package com.myce.member.service;

import com.myce.advertisement.dto.AdRejectInfoResponse;
import com.myce.member.dto.ad.AdRefundReceiptResponse;
import com.myce.member.dto.ad.AdRefundRequest;
import com.myce.member.dto.ad.AdvertisementDetailResponse;
import com.myce.member.dto.ad.AdvertisementPaymentDetailResponse;
import com.myce.member.dto.ad.AdvertisementRefundReceiptResponse;
import com.myce.member.dto.ad.MemberAdvertisementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberAdService {
    
    Page<MemberAdvertisementResponse> getMemberAdvertisements(Long memberId, Pageable pageable);
    
    AdvertisementDetailResponse getAdvertisementDetail(Long memberId, Long advertisementId);
    
    void cancelAdvertisement(Long memberId, Long advertisementId);
    
    void cancelByStatus(Long memberId, Long advertisementId);
    
    void requestRefundByStatus(Long memberId, Long advertisementId, AdRefundRequest request);
    
    AdvertisementPaymentDetailResponse getAdvertisementPaymentDetail(Long memberId, Long advertisementId);
    
    AdvertisementRefundReceiptResponse getAdvertisementRefundReceipt(Long memberId, Long advertisementId);
    
    AdRejectInfoResponse getAdvertisementRejectInfo(Long memberId, Long advertisementId);
    
    AdRefundReceiptResponse getAdvertisementRefundHistory(Long memberId, Long advertisementId);
    
    void completeAdvertisementPayment(Long memberId, Long advertisementId);
}