package com.myce.payment.service.refund;

import com.myce.payment.dto.PaymentImpUidForRefundRequest;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.AdRefundRequest;
import java.util.Map;

public interface PaymentRefundService {
    Map<String, Object> refundPayment(PaymentRefundRequest request);

    String getImpUidForRefund(PaymentImpUidForRefundRequest request);
    
    Map<String, Object> processAdRefund(AdRefundRequest request);
}
