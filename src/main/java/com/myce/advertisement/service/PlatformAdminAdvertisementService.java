package com.myce.advertisement.service;

import com.myce.advertisement.dto.AdRejectInfoResponse;
import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.RejectAdRequest;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.common.dto.PageResponse;

public interface PlatformAdminAdvertisementService {
    PageResponse<SimpleApplyAdvertisement> getAllAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply);

    PageResponse<SimpleApplyAdvertisement> getFilteredAdListByKeyword(
            String keyword, String status,
            int page, int pageSize, boolean latestFirst, boolean isApply);

    DetailApplyAdvertisement getDetail(Long bannerId);

    void rejectApply(Long bannerId, RejectAdRequest request);

    AdRejectInfoResponse getRejectInfo(Long bannerId);
}
