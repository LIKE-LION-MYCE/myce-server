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
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.RefundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PlatformCurrentAdServiceImpl implements PlatformCurrentAdService {
    private final AdRepository adRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    public AdCancelInfoCheck generateCancelCheck(Long adId) {
        Advertisement ad = adRepository
                .findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(ad.getId(), PaymentTargetType.AD)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        AdPaymentInfo adPayment = adPaymentInfoRepository
                .findByAdvertisementId(ad.getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        long remainDays = LocalDate.now().until(ad.getDisplayEndDate(), ChronoUnit.DAYS);
        Integer totalAmount = (int) remainDays * adPayment.getFeePerDay();
        return AdInfoMapper.getAdCancelInfoCheck(payment, ad, totalAmount);
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
        long remainDays = LocalDate.now().until(ad.getDisplayEndDate(), ChronoUnit.DAYS);
        Integer totalAmount = (int) remainDays * adPayment.getFeePerDay();

        Refund refund = Refund.builder()
                .isPartial(true)
                .payment(payment)
                .amount(totalAmount)
                .refundedAt(LocalDateTime.now())
                .status(RefundStatus.PENDING)
                .build();
        refundRepository.save(refund);

        ad.cancel();
    }
}
