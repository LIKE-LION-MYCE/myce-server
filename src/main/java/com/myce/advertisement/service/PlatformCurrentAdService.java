package com.myce.advertisement.service;

import com.myce.advertisement.dto.AdCancelInfoCheck;

public interface PlatformCurrentAdService {
    void cancelCurrent(Long adId);

    void denyCancel(Long adId);

    AdCancelInfoCheck generateCancelCheck(Long adId);
}
