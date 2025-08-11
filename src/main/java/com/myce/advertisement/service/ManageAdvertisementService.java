package com.myce.advertisement.service;


import com.myce.advertisement.dto.MainPageAdInfo;

import java.time.LocalDate;
import java.util.List;

public interface ManageAdvertisementService {
    void checkAvailablePeriod(Long locationId,
            LocalDate startedAt, LocalDate endedAt);

    List<MainPageAdInfo> getActiveBanners();

    int publishPendingAds();

    int closeCompletedAds();

    void refreshBannerCache();
}