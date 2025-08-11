package com.myce.payment.service.impl;

import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.PortOneWebhookRequest;
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

    @Override
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        return paymentVerificationService.verifyPayment(request);
    }

    @Override
    @Transactional
    public Map<String, Object> refundPayment(PaymentRefundRequest request) {
        return paymentRefundService.refundPayment(request);
    }

    @Override
    @Transactional
    public PaymentVerifyResponse verifyVbankPayment(PaymentVerifyRequest request) {
        return paymentVerificationService.verifyVbankPayment(request);
    }

    @Override
    @Transactional
    public void processWebhook(PortOneWebhookRequest request) {
        paymentWebhookService.processWebhook(request);
    }
}
