package com.myce.payment.service.verification;

import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;

public interface PaymentVerificationService {
    PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);
    PaymentVerifyResponse verifyVbankPayment(PaymentVerifyRequest request);
}
