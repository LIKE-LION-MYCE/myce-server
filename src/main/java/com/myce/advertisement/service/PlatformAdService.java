package com.myce.advertisement.service;

import com.myce.advertisement.dto.*;
import com.myce.common.dto.PageResponse;

public interface PlatformAdService {
    PageResponse<AdSimpleResponse> getAllAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply);

    PageResponse<AdSimpleResponse> getFilteredAdListByKeyword(
            String keyword, String status,
            int page, int pageSize, boolean latestFirst, boolean isApply);
}
