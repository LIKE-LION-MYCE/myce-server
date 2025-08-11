package com.myce.advertisement.service;


import com.myce.advertisement.dto.AdMainPageInfo;

import java.time.LocalDate;
import java.util.List;

public interface SystemAdService {
    void checkAvailablePeriod(Long locationId,
            LocalDate startedAt, LocalDate endedAt);

    List<AdMainPageInfo> getActiveBanners();

    int publishPendingAds();

    int closeCompletedAds();

    void refreshBannerCache();
}