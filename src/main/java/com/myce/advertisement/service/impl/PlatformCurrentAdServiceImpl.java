package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.AdCancelInfoCheck;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdRepository;
import com.myce.advertisement.service.PlatformCurrentAdService;
import com.myce.advertisement.service.mapper.AdInfoMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformCurrentAdServiceImpl implements PlatformCurrentAdService {
    private final AdRepository adRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    public AdCancelInfoCheck generateCancelCheck(Long adId) {
        log.info("generateCancelCheck - Advertisement Id : {}, targetType : {}", adId, PaymentTargetType.AD.name());
        Advertisement ad = adRepository
                .findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(ad.getId(), PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        Refund refund = refundRepository.findByPayment(payment)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REFUND_NOT_FOUND));
        Integer totalAmount = refund.getAmount();
        log.info("generateCancelCheck - Advertisement : {}, Payment : {}", ad, payment);
        return AdInfoMapper.getAdCancelInfoCheck(payment, ad, totalAmount);
    }

    @Transactional
    public void denyCancel(Long adId){
        Advertisement ad = adRepository
                .findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(ad.getId(), PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        ad.denyCancel();

        refundRepository.deleteByPayment(payment);
    }

    @Transactional
    public void cancelCurrent(Long adId) {
        Advertisement ad = adRepository
                .findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(ad.getId(), PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        AdPaymentInfo adPayment = adPaymentInfoRepository
                .findByAdvertisementId(ad.getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        Refund refund = refundRepository.findByPayment(payment)
                .orElseThrow(() -> new CustomException(CustomErrorCode.REFUND_NOT_FOUND));

        if(refund.getIsPartial()){
            adPayment.setStatus(PaymentStatus.PARTIAL_REFUNDED);
        }else{
            adPayment.setStatus(PaymentStatus.REFUNDED);
        }
        adPayment.setUpdatedAt(LocalDateTime.now());

        log.info("cancelCurrent - Advertisement : {}, Payment : {}", ad, payment);
        log.info("refund.isPartial = {}", refund.getIsPartial());

        refund.updateToRefund();
        ad.cancel();
    }
}
