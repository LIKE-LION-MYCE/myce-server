package com.myce.advertisement.service;

import com.myce.advertisement.dto.*;

public interface PlatformApplyAdService {
    AdPaymentInfoCheck generatePaymentCheck(Long adId);

    void approveApply(Long adId);

    void rejectApply(Long adId, AdRejectRequest request);

    AdRejectInfoResponse getRejectInfo(Long adId);

    AdPaymentHistoryResponse getPaymentHistory(Long adId);

    AdCancelHistoryResponse getCancelHistory(Long adId);
}
