package com.myce.payment.service.verification;

import com.myce.payment.dto.PaymentVerifyInfo;
import com.myce.payment.dto.PaymentVerifyResponse;

public interface PaymentVerificationService {
    PaymentVerifyResponse verifyPayment(PaymentVerifyInfo request);
    PaymentVerifyResponse verifyVbankPayment(PaymentVerifyInfo request);
}
