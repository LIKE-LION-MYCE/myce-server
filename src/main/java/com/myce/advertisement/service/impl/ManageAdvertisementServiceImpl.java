package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.MainPageAdInfo;
import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdPositionRepository;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.advertisement.service.ManageAdvertisementService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManageAdvertisementServiceImpl implements ManageAdvertisementService {
    private final AdvertisementRepository adRepository;
    private final AdPositionRepository adPositionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void checkAvailablePeriod(Long locationId,
                                     LocalDate startedAt, LocalDate endedAt) {
        AdPosition requestedAdPosition = adPositionRepository.findById(locationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BANNER_POSITION_NOT_EXIST));
        List<AdvertisementStatus> activeStatusList = getActiveStatusList();

        List<Advertisement> activeAds = adRepository.findOverlappingAds(
                startedAt, endedAt, activeStatusList, locationId);

        LocalDate date;
        for (date = startedAt; date.isBefore(endedAt.plusDays(1)); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            long overlappingCount = activeAds.stream()
                    .filter(ad -> !finalDate.isBefore(ad.getDisplayStartDate()) &&
                            !finalDate.isAfter(ad.getDisplayEndDate()))
                    .count();

            if (overlappingCount >= requestedAdPosition.getMaxCount()) {
                throw new CustomException(CustomErrorCode.BANNER_MAX_CAPACITY_REACHED);
            }
        }
    }

    // 게시중인 배너 조회
    public List<MainPageAdInfo> getActiveBanners() {
        List<Object> banners = redisTemplate.opsForList().range("banner:*", 0, -1);

        return Objects.requireNonNull(banners).stream()
                .map(banner -> (MainPageAdInfo) banner)
                .collect(Collectors.toList());
    }

    // 게시중인 배너 수집(업데이트 날짜가 오늘이 아니면)
    @PostConstruct
    public void checkForBannerUpdates() {
        System.out.println("ManageAdvertisementServiceImpl.checkForBannerUpdates");
        LocalDate today = LocalDate.now();
        LocalDate lastCacheUpdateTime = (LocalDate) redisTemplate.opsForValue().get("banner:lastUpdateTime");

        if (lastCacheUpdateTime == null || today.isAfter(lastCacheUpdateTime)) {
            System.out.println("update banner cache");
            refreshBannerCache();
        }
    }

    private void refreshBannerCache() {
        System.out.println("ManageAdvertisementServiceImpl.refreshBannerCache");
        List<AdvertisementStatus> activeStatusList = getActiveStatusList();
        List<Advertisement> allPublishedAds = adRepository
                .findAllPublishedAdvertisements(activeStatusList);

        redisTemplate.delete("banner:*");
        for (Advertisement ad : allPublishedAds) {
            MainPageAdInfo adInfo = MainPageAdInfo.builder()
                    .bannerId(ad.getId())
                    .locationId(ad.getAdPosition().getId())
                    .bannerImageUrl(ad.getImageUrl())
                    .linkUrl(ad.getLinkUrl())
                    .build();

            String redisKey = "banner:" + adInfo.getLocationId();
            redisTemplate.opsForList().rightPush(redisKey, adInfo);
        }

        redisTemplate.opsForValue().set("banner:lastUpdateTime", LocalDate.now());
    }

    private static List<AdvertisementStatus> getActiveStatusList() {
        return List.of(AdvertisementStatus.PUBLISHED,
                AdvertisementStatus.PENDING_CANCEL);
    }
}
