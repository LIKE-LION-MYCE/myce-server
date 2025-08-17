package com.myce.advertisement.service;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.common.dto.PageResponse;

public interface PlatformAdService {
    PageResponse<AdResponse> getAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply);

    PageResponse<AdResponse> getFilteredAdListByKeyword(
            String keyword, String status,
            int page, int pageSize, boolean latestFirst, boolean isApply);

    void updateAdStatus(Long adId, AdvertisementStatus advertisementStatus);
}
