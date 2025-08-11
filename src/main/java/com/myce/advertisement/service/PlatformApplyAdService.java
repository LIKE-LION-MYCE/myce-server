package com.myce.advertisement.service;

import com.myce.advertisement.dto.*;

public interface PlatformApplyAdService {
    AdPaymentInfoCheck generatePaymentCheck(Long bannerId);

    void approveApply(Long bannerId, AdPaymentInfoRequest paymentInfoRequest);

    void rejectApply(Long bannerId, AdRejectRequest request);

    AdRejectInfoResponse getRejectInfo(Long bannerId);

    AdPaymentHistoryResponse getPaymentHistory(Long bannerId);

    AdCancelHistoryResponse getCancelHistory(Long bannerId);
}
