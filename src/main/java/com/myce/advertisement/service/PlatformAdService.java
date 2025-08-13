package com.myce.advertisement.service;

import com.myce.advertisement.dto.*;
import com.myce.common.dto.PageResponse;

public interface PlatformAdService {
    PageResponse<AdResponse> getAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply);

    PageResponse<AdResponse> getFilteredAdListByKeyword(
            String keyword, String status,
            int page, int pageSize, boolean latestFirst, boolean isApply);
}
