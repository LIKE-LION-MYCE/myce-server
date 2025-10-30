package com.myce.payment.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PaymentVerifyInfo;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.PortOneWebhookRequest;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.service.PaymentService;
import com.myce.payment.service.refund.PaymentRefundService;
import com.myce.payment.service.verification.PaymentVerificationService;
import com.myce.payment.service.webhook.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentVerificationService paymentVerificationService;
    private final PaymentRefundService paymentRefundService;
    private final PaymentWebhookService paymentWebhookService;
    private final AdPaymentInfoRepository adPaymentInfoRepository;

    @Override
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyInfo request) {
        return paymentVerificationService.verifyPayment(request);
    }

    @Override
    @Transactional
    public Map<String, Object> refundPayment(PaymentRefundRequest request) {
        return paymentRefundService.refundPayment(request);
    }

    @Override
    @Transactional
    public PaymentVerifyResponse verifyVbankPayment(PaymentVerifyInfo request) {
        return paymentVerificationService.verifyVbankPayment(request);
    }

    @Override
    @Transactional
    public void processWebhook(PortOneWebhookRequest request) {
        paymentWebhookService.processWebhook(request);
    }

    @Override
    @Transactional
    public void updateAdPaymentInfo(Long adId, PaymentStatus paymentStatus) {
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findById(adId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        adPaymentInfo.updateStatus(paymentStatus);
    }
}
