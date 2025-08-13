package com.myce.member.service.impl;

import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdRepository;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.member.dto.ad.AdRefundRequest;
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
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

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
    @Transactional
    public void cancelByStatus(Long memberId, Long advertisementId) {
        Advertisement advertisement = adRepository.findByIdAndMemberIdWithAdPosition(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        
        // 상태별 취소 처리
        AdvertisementStatus currentStatus = advertisement.getStatus();
        advertisement.cancelByStatus();
        
        // PENDING_PAYMENT 상태였다면 AdPaymentInfo도 REFUNDED로 변경
        if (currentStatus == AdvertisementStatus.PENDING_PAYMENT) {
            AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
            adPaymentInfo.setStatus(PaymentStatus.REFUNDED);
        }
    }
    
    
    @Override
    @Transactional
    public void requestRefundByStatus(Long memberId, Long advertisementId, AdRefundRequest request) {
        Advertisement advertisement = adRepository.findByIdAndMemberIdWithAdPosition(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        
        // AdPaymentInfo 조회
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        // Payment 조회 (AD 타입, advertisementId)
        Payment payment = paymentRepository.findByTargetIdAndTargetType(advertisementId, PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        
        // 이미 환불 신청이 있는지 확인
        if (refundRepository.findByPayment(payment).isPresent()) {
            throw new CustomException(CustomErrorCode.ALREADY_REFUND_REQUESTED);
        }
        
        // 광고 상태에 따른 처리
        AdvertisementStatus currentStatus = advertisement.getStatus();
        advertisement.requestRefundByStatus();
        
        Integer refundAmount;
        boolean isPartial;
        
        switch (currentStatus) {
            case PENDING_PUBLISH:
                // 게시 예정 - 전액 환불
                refundAmount = adPaymentInfo.getTotalAmount();
                isPartial = false;
                break;
            case PUBLISHED:
                // 게시 중 - 부분 환불 (남은 일수만큼)
                LocalDate today = LocalDate.now();
                LocalDate endDate = advertisement.getDisplayEndDate();
                long remainingDays = ChronoUnit.DAYS.between(today, endDate);
                
                if (remainingDays < 0) {
                    remainingDays = 0;
                }
                
                refundAmount = (int) (remainingDays * adPaymentInfo.getFeePerDay());
                isPartial = true;
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        
        // 환불 신청 생성
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(refundAmount)
                .reason(request.getReason())
                .status(RefundStatus.PENDING)
                .isPartial(isPartial)
                .refundedAt(null)
                .build();
        
        refundRepository.save(refund);
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