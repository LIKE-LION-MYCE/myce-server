package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdRepository;
import com.myce.advertisement.service.PlatformApplyAdService;
import com.myce.advertisement.service.mapper.AdInfoMapper;
import com.myce.common.entity.RejectInfo;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.RejectInfoRepository;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import com.myce.system.entity.AdFeeSetting;
import com.myce.system.repository.AdFeeSettingRepository;
import com.myce.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformApplyAdServiceImpl implements PlatformApplyAdService {
    private final AdRepository adRepository;
    private final RejectInfoRepository rejectInfoRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final AdFeeSettingRepository adFeeSettingRepository;
    private final NotificationService notificationService;

    public AdPaymentInfoCheck generatePaymentCheck(Long adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        AdFeeSetting feeSetting = adFeeSettingRepository
                .findByAdPositionIdAndIsActiveTrue(ad.getAdPosition().getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
        HashMap<String, Integer> priceMap = new HashMap<>();
        int totalPayment = 0;

        int feePerDay = feeSetting.getFeePerDay();
        int totalDayFee = feePerDay * ad.getTotalDays();
        int totalDays = ad.getTotalDays();

        log.info("generatePaymentCheck - Advertisement : {}, {}", feePerDay, ad.getTotalDays());

        // todo: PG 수수료 고려 X
        priceMap.put("일일 이용료", feePerDay);
        totalPayment += totalDayFee;

        return AdInfoMapper.getAdPaymentForm(ad, priceMap, totalDays, totalPayment);
    }

    @Transactional
    public void approveApply(Long adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        AdFeeSetting feeSetting = adFeeSettingRepository
                .findByAdPositionIdAndIsActiveTrue(ad.getAdPosition().getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));

        AdPaymentInfo paymentInfo = AdPaymentInfo.builder()
                .advertisement(ad)
                .status(PaymentStatus.PENDING)
                .totalAmount(feeSetting.getFeePerDay() * ad.getTotalDays())
                .totalDay(ad.getTotalDays())
                .feePerDay(feeSetting.getFeePerDay())
                .build();

        log.info("approveApply - Advertisement : {}", ad);
        adPaymentInfoRepository.save(paymentInfo);
        
        String oldStatus = ad.getStatus().name();
        ad.approve();
        String newStatus = ad.getStatus().name();
        
        // 상태 변경 알림 전송
        try {
            notificationService.sendAdvertisementStatusChangeNotification(adId, ad.getTitle(), oldStatus, newStatus);
        } catch (Exception e) {
            log.warn("광고 승인 알림 전송 실패 - adId: {}, 오류: {}", adId, e.getMessage());
        }
    }

    @Transactional
    public void rejectApply(Long adId, AdRejectRequest request) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        RejectInfo rejectInfo = RejectInfo.builder()
                .targetType(TargetType.ADVERTISEMENT)
                .targetId(ad.getId())
                .description(request.getReason())
                .build();

        log.info("rejectApply - Advertisement : {}", ad);

        rejectInfoRepository.save(rejectInfo);
        
        String oldStatus = ad.getStatus().name();
        ad.reject();
        String newStatus = ad.getStatus().name();
        
        // 상태 변경 알림 전송
        try {
            notificationService.sendAdvertisementStatusChangeNotification(adId, ad.getTitle(), oldStatus, newStatus);
        } catch (Exception e) {
            log.warn("광고 거절 알림 전송 실패 - adId: {}, 오류: {}", adId, e.getMessage());
        }
    }

    public AdRejectInfoResponse getRejectInfo(Long adId) {
        RejectInfo rejectInfo = rejectInfoRepository
                .findByTargetIdAndTargetType(adId, TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REJECT_INFO_NOT_FOUND));

        log.info("getRejectInfo - RejectInfo : {}", rejectInfo);
        return AdInfoMapper.getAdRejectInfoResponse(rejectInfo);
    }

    public AdPaymentHistoryResponse getPaymentHistory(Long adId) {
        AdPaymentInfo paymentInfo = adPaymentInfoRepository
                .findByAdvertisementId(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(adId, PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        log.info("getPaymentHistory - AdPaymentInfo : {}", paymentInfo);
        return AdInfoMapper.getPaymentInfoResponse(paymentInfo, payment);
    }

    public AdCancelHistoryResponse getCancelHistory(Long adId) {
        Advertisement advertisement = adRepository
                .findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(adId, PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        Refund refund = refundRepository
                .findByPayment(payment)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REFUND_NOT_FOUND));
        log.info("getCancelHistory - Advertisement : {}", advertisement);
        return AdInfoMapper.getAdCancelInfoResponse(advertisement, payment, refund);
    }
}
