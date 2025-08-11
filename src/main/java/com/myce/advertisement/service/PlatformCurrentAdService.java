package com.myce.advertisement.service;

import com.myce.advertisement.dto.AdCancelInfoCheck;
import com.myce.advertisement.dto.AdCancelInfoRequest;

public interface PlatformCurrentAdService {
    void cancelBanner(Long bannerId, AdCancelInfoRequest request);

    AdCancelInfoCheck generateCancelCheck(Long bannerId);
}
