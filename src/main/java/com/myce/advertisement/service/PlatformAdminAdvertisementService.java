package com.myce.advertisement.service;

import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.common.dto.PageResponse;

public interface PlatformAdminAdvertisementService {
    PageResponse<SimpleApplyAdvertisement> getList(int page, int pageSize, boolean latestFirst);

    PageResponse<SimpleApplyAdvertisement> filterList(String keyword, String status, int page, int pageSize, boolean latestFirst);

    DetailApplyAdvertisement getDetail(Long bannerId);
}
