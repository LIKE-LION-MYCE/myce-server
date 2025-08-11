package com.myce.advertisement.service;

import com.myce.advertisement.dto.*;
import com.myce.common.dto.PageResponse;

public interface PlatformAdminAdvertisementService {
    PageResponse<SimpleApplyAdvertisement> getAllAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply);

    PageResponse<SimpleApplyAdvertisement> getFilteredAdListByKeyword(
            String keyword, String status,
            int page, int pageSize, boolean latestFirst, boolean isApply);

    DetailApplyAdvertisement getDetail(Long bannerId);

    AdPaymentInfoCheck generatePaymentCheck(Long bannerId);

    void approveApply(Long bannerId, AdPaymentInfoRequest paymentInfoRequest);

    void rejectApply(Long bannerId, RejectAdRequest request);

    AdCancelInfoCheck generateCancelCheck(Long bannerId);

    void cancelBanner(Long bannerId, AdCancelInfoRequest request);

    AdRejectInfoResponse getRejectInfo(Long bannerId);

    AdPaymentHistoryResponse getPaymentInfo(Long bannerId);

    AdCancelHistoryResponse getCancelInfo(Long bannerId);
}
