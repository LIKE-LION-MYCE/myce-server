package com.myce.payment.service;

import com.myce.common.dto.PageResponse;
import com.myce.payment.dto.PaymentInfoResponse;

public interface PaymentInfoPlatformService {
    PageResponse<PaymentInfoResponse> getPaymentInfoPage(Integer page, Integer size,
            boolean latestFirst);

    PageResponse<PaymentInfoResponse> filterPaymentInfoPage(Integer page, Integer size,
            boolean latestFirst, String status, String keyword);
}
