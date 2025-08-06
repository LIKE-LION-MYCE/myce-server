package com.myce.advertisement.service;

import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.common.dto.PageResponse;

public interface PlatformAdminAdvertisementService {
    PageResponse<SimpleApplyAdvertisement> getAllAdvertisementList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply);

    PageResponse<SimpleApplyAdvertisement> getFilteredAdvertisementListByKeyword(
            String keyword, String status,
            int page, int pageSize, boolean latestFirst, boolean isApply);

    DetailApplyAdvertisement getDetail(Long bannerId);
}
