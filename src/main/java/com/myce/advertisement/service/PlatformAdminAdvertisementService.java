package com.myce.advertisement.service;

import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.common.dto.PageResponse;

public interface PlatformAdminAdvertisementService {
    PageResponse<SimpleApplyAdvertisement> getList(int page, int pageSize, boolean latestFirst);

    PageResponse<SimpleApplyAdvertisement> getFiltersByKeyword(String keyword, String status,
                                                               int page, int pageSize, boolean latestFirst);
}
