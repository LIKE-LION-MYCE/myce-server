package com.myce.payment.service.portone;

import com.myce.payment.dto.PaymentRefundRequest;
import java.util.Map;

public interface PortOneApiService {
    String getAccessToken();

    Map<String, Object> getPaymentInfo(String impUid);

    Map<String, Object> requestRefundToPortOne(PaymentRefundRequest request, Integer originalPaidAmount, String accessToken);
}
