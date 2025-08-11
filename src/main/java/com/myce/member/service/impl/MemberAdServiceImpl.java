package com.myce.member.service.impl;

import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdRepository;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.member.dto.*;
import com.myce.member.mapper.*;
import com.myce.member.service.MemberAdService;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.repository.AdPaymentInfoRepository;
import lombok.RequiredArgsConstructor;
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
    public List<MemberAdvertisementResponse> getMemberAdvertisements(Long memberId) {
        List<Advertisement> advertisements = adRepository.findByMemberIdWithAdPosition(memberId);
        return memberAdvertisementMapper.toResponseDtoList(advertisements);
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

        // 게시 중인 광고만 환불 가능
        if (advertisement.getStatus() != com.myce.advertisement.entity.type.AdvertisementStatus.PUBLISHED) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }

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