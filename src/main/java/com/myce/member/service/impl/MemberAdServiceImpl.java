package com.myce.member.service.impl;

import com.myce.advertisement.dto.AdRejectInfoResponse;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdRepository;
import com.myce.advertisement.service.SystemAdService;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.RejectInfo;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.repository.RejectInfoRepository;
import com.myce.member.dto.ad.AdRefundReceiptResponse;
import com.myce.member.dto.ad.AdRefundRequest;
import com.myce.member.dto.ad.AdvertisementDetailResponse;
import com.myce.member.dto.ad.AdvertisementPaymentDetailResponse;
import com.myce.member.dto.ad.AdvertisementRefundReceiptResponse;
import com.myce.member.dto.ad.MemberAdvertisementResponse;
import com.myce.member.mapper.ad.AdRefundReceiptMapper;
import com.myce.member.mapper.ad.AdvertisementDetailMapper;
import com.myce.member.mapper.ad.AdvertisementPaymentDetailMapper;
import com.myce.member.mapper.ad.AdvertisementRefundReceiptMapper;
import com.myce.member.mapper.ad.MemberAdvertisementMapper;
import com.myce.member.service.MemberAdService;
import com.myce.notification.service.NotificationService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAdServiceImpl implements MemberAdService {

    private final AdRepository adRepository;
    private final MemberAdvertisementMapper memberAdvertisementMapper;
    private final AdvertisementDetailMapper advertisementDetailMapper;
    private final AdvertisementPaymentDetailMapper advertisementPaymentDetailMapper;
    private final AdvertisementRefundReceiptMapper advertisementRefundReceiptMapper;
    private final AdRefundReceiptMapper adRefundReceiptMapper;
    private final BusinessProfileRepository businessProfileRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RejectInfoRepository rejectInfoRepository;
    private final NotificationService notificationService;
    private final SystemAdService systemAdService;

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
        
        AdvertisementStatus currentStatus = advertisement.getStatus();
        
        // 상태별 취소 처리
        switch (currentStatus) {
            case PENDING_APPROVAL:
                // 승인대기 상태: status만 CANCELLED로 변경 (AdPaymentInfo 없음)
                advertisement.updateStatus(AdvertisementStatus.CANCELLED);

                try {
                    notificationService.sendExpoStatusChangeNotification(advertisementId, advertisement.getTitle(), "PENDING_APPROVAL",
                            "CANCELLED");
                } catch (Exception e) {
                    log.warn("박람회 결제 완료 알림 전송 실패 - expoId: {}, 오류: {}", advertisementId, e.getMessage());
                }

                log.info("승인대기 취소 - 광고 상태만 변경: 광고 ID {}", advertisementId);
                break;
                
            case PENDING_PAYMENT:
                // 결제대기 상태: status CANCELLED + AdPaymentInfo 삭제
                advertisement.updateStatus(AdvertisementStatus.CANCELLED);
                AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                        .orElse(null);
                if (adPaymentInfo != null) {
                    adPaymentInfoRepository.delete(adPaymentInfo);
                    log.info("결제대기 취소 - AdPaymentInfo 삭제됨: 광고 ID {}", advertisementId);
                }
                try {
                    notificationService.sendExpoStatusChangeNotification(advertisementId, advertisement.getTitle(), "PENDING_PAYMENT",
                            "CANCELLED");
                } catch (Exception e) {
                    log.warn("박람회 결제 완료 알림 전송 실패 - expoId: {}, 오류: {}", advertisementId, e.getMessage());
                }

                break;
                
            case PENDING_PUBLISH:
                // 게시예정 상태: status CANCELLED + AdPaymentInfo 삭제
                advertisement.updateStatus(AdvertisementStatus.CANCELLED);
                AdPaymentInfo pendingPublishPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                        .orElse(null);
                if (pendingPublishPaymentInfo != null) {
                    adPaymentInfoRepository.delete(pendingPublishPaymentInfo);
                    log.info("게시예정 취소 - AdPaymentInfo 삭제됨: 광고 ID {}", advertisementId);
                }
                try {
                    notificationService.sendExpoStatusChangeNotification(advertisementId, advertisement.getTitle(), "PENDING_PUBLISH",
                            "CANCELLED");
                } catch (Exception e) {
                    log.warn("박람회 결제 완료 알림 전송 실패 - expoId: {}, 오류: {}", advertisementId, e.getMessage());
                }

                break;
                
            default:
                // 기존 로직 (다른 상태들)
                advertisement.cancelByStatus();
                break;
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
        String oldStatus = currentStatus.name();

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
                LocalDate startDate = advertisement.getDisplayStartDate();
                
                // 영수증과 동일한 계산 방식: 사용한 일수 계산 후 차감
                int usedDays = (int) ChronoUnit.DAYS.between(startDate, today) + 1; // +1은 시작일 포함
                if (usedDays < 0) usedDays = 0;
                
                // 남은 일수 계산
                int remainingDays = adPaymentInfo.getTotalDay() - usedDays;
                if (remainingDays < 0) remainingDays = 0;
                
                refundAmount = remainingDays * adPaymentInfo.getFeePerDay();
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

        try {
            notificationService.sendAdvertisementStatusChangeNotification(
                    advertisementId, advertisement.getTitle(), oldStatus, "PENDING_CANCEL");
        } catch (Exception e) {
            log.warn("광고 환불 신청 알림 전송 실패 - adId: {}, 오류: {}", advertisementId, e.getMessage());
        }
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
    
    @Override
    public AdRejectInfoResponse getAdvertisementRejectInfo(Long memberId, Long advertisementId) {
        // 광고가 해당 회원의 것인지 확인
        Advertisement advertisement = adRepository.findByIdAndMemberId(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        
        // 거절 정보 조회
        RejectInfo rejectInfo = rejectInfoRepository.findByTargetIdAndTargetType(advertisementId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REJECT_INFO_NOT_FOUND));
        
        return AdRejectInfoResponse.builder()
                .description(rejectInfo.getDescription())
                .rejectedAt(rejectInfo.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void completeAdvertisementPayment(Long memberId, Long advertisementId) {
        
        // 광고 조회 및 권한 확인
        Advertisement advertisement = adRepository.findByIdAndMemberId(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));

        // 1. 광고 상태를 PENDING_PUBLISH로 변경
        advertisement.updateStatus(AdvertisementStatus.PENDING_PUBLISH);
        adRepository.save(advertisement);

        // 2. AdPaymentInfo 상태를 PENDING에서 SUCCESS로 업데이트
        AdPaymentInfo paymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        paymentInfo.updateStatus(PaymentStatus.SUCCESS);
        adPaymentInfoRepository.save(paymentInfo);

        try {
            notificationService.sendAdvertisementStatusChangeNotification(
                    advertisementId, advertisement.getTitle(), "PENDING_PAYMENT", "PENDING_PUBLISH");
        } catch (Exception e) {
            log.warn("광고 환불 신청 알림 전송 실패 - adId: {}, 오류: {}", advertisementId, e.getMessage());
        }

        systemAdService.updateAdStatus();
        log.info("광고 결제 완료 처리 - 광고 ID: {}, 회원 ID: {}", advertisementId, memberId);
    }
    
    @Override
    public AdRefundReceiptResponse getAdvertisementRefundHistory(Long memberId, Long advertisementId) {
        // 광고가 해당 회원의 것인지 확인
        Advertisement advertisement = adRepository.findByIdAndMemberId(advertisementId, memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));

        Payment payment = paymentRepository.findByTargetIdAndTargetType(advertisementId, PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        // 비즈니스 프로필 조회
        BusinessProfile businessProfile = businessProfileRepository.findByTargetIdAndTargetType(advertisementId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));
        
        // 광고 결제 정보 조회
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        
        // 실제 환불 내역 조회 (REFUND 테이블에서)
        Refund refund = refundRepository.findByPayment(payment)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REFUND_NOT_FOUND));

        
        return adRefundReceiptMapper.toRefundHistoryDto(advertisement, businessProfile, adPaymentInfo, refund);
    }
}