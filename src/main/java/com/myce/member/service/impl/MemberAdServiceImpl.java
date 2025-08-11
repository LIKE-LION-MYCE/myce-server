package com.myce.member.service.impl;

import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdRepository;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.member.dto.ad.AdvertisementDetailResponse;
import com.myce.member.dto.ad.AdvertisementPaymentDetailResponse;
import com.myce.member.dto.ad.AdvertisementRefundReceiptResponse;
import com.myce.member.dto.ad.MemberAdvertisementResponse;
import com.myce.member.mapper.ad.AdvertisementDetailMapper;
import com.myce.member.mapper.ad.AdvertisementPaymentDetailMapper;
import com.myce.member.mapper.ad.AdvertisementRefundReceiptMapper;
import com.myce.member.mapper.ad.MemberAdvertisementMapper;
import com.myce.member.service.MemberAdService;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.repository.AdPaymentInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAdServiceImpl implements MemberAdService {

    private final AdRepository adRepository;
    private final MemberAdvertisementMapper memberAdvertisementMapper;
    private final AdvertisementDetailMapper advertisementDetailMapper;
    private final AdvertisementPaymentDetailMapper advertisementPaymentDetailMapper;
    private final AdvertisementRefundReceiptMapper advertisementRefundReceiptMapper;
    private final BusinessProfileRepository businessProfileRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;

    @Override
    public Page<MemberAdvertisementResponse> getMemberAdvertisements(Long memberId, Pageable pageable) {
        Page<Advertisement> advertisements = adRepository.findByMemberIdWithAdPosition(memberId, pageable);
        return advertisements.map(memberAdvertisementMapper::toResponseDto);
    }

    @Override
    public AdvertisementDetailResponse getAdvertisementDetail(Long memberId, Long advertisementId) {

        Advertisement advertisement = adRepository.findByIdAndMemberIdWithAdPosition(advertisementId,
                        memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));

        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(advertisementId,
                        TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return advertisementDetailMapper.toResponseDto(advertisement, businessProfile);
    }

    @Override
    @Transactional
    public void cancelAdvertisement(Long memberId, Long advertisementId) {
        Advertisement advertisement = adRepository.findByIdAndMemberIdWithAdPosition(advertisementId,
                        memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));

        advertisement.cancel();
    }

    @Override
    public AdvertisementPaymentDetailResponse getAdvertisementPaymentDetail(Long memberId, Long advertisementId) {
        // 광고 정보 조회
        Advertisement advertisement = adRepository.findByIdAndMemberIdWithAdPosition(advertisementId,
                        memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));

        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(advertisementId,
                        TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        // 광고 결제 정보 조회 (AdPaymentInfo 테이블에서)
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        return advertisementPaymentDetailMapper.toAdvertisementPaymentDetailResponse(advertisement, businessProfile, adPaymentInfo);
    }

    @Override
    public AdvertisementRefundReceiptResponse getAdvertisementRefundReceipt(Long memberId, Long advertisementId) {
        // 광고 정보 조회
        Advertisement advertisement = adRepository.findByIdAndMemberIdWithAdPosition(advertisementId,
                        memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));

        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(advertisementId,
                        TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        // 광고 결제 정보 조회
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        return advertisementRefundReceiptMapper.toRefundReceiptDto(advertisement, businessProfile, adPaymentInfo);
    }
}