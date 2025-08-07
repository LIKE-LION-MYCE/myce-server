package com.myce.advertisement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

        //기간 별로 최대 조건을 넘는지 탐색
        LocalDate date;
        for (date = startedAt; date.isBefore(endedAt.plusDays(1)); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            long overlappingCount = activeAds.stream()
                    .filter(ad -> !finalDate.isBefore(ad.getDisplayStartDate()) &&
                            !finalDate.isAfter(ad.getDisplayEndDate()))
                    .count();
            //해당 날짜의 배너 수가 최댓값일때
            if (overlappingCount >= requestedAdPosition.getMaxCount()) {
                throw new CustomException(CustomErrorCode.BANNER_MAX_CAPACITY_REACHED);
            }
        }
    }

    // 게시중인 배너 조회
    public List<MainPageAdInfo> getActiveBanners() {
        Set<String> keys = redisTemplate.keys("banner:list:*");
        List<Object> totalBanners = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String key : keys) {
            List<Object> banners = redisTemplate.opsForList().range(key, 0, -1);
            if(banners != null && !banners.isEmpty()) {
                totalBanners.addAll(banners);
            }
        }

        return Objects.requireNonNull(totalBanners).stream()
                .map(banner -> objectMapper.convertValue(banner, MainPageAdInfo.class))
                .collect(Collectors.toList());
    }

    // 게시중인 배너 수집(업데이트 날짜가 오늘이 아니면)
    @PostConstruct
    public void checkForBannerUpdates() {
        LocalDate today = LocalDate.now();
        String lastUpdateTimeString = (String) redisTemplate.opsForValue().get("banner:lastUpdateTime");

        if (lastUpdateTimeString == null
                || today.isAfter(LocalDate.parse(lastUpdateTimeString, DateTimeFormatter.ISO_DATE))) {
            refreshBannerCache();
        }
    }

    private void refreshBannerCache() {
        List<AdvertisementStatus> activeStatusList = getActiveStatusList();
        List<Advertisement> allPublishedAds = adRepository
                .findAllPublishedAdvertisements(activeStatusList);

        Set<String> keys = redisTemplate.keys("banner:list:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        for (Advertisement ad : allPublishedAds) {
            MainPageAdInfo adInfo = new MainPageAdInfo(
                    ad.getId(),
                    ad.getAdPosition().getId(),
                    ad.getImageUrl(),
                    ad.getLinkUrl());

            String redisKey = "banner:list:" + adInfo.getLocationId();
            redisTemplate.opsForList().rightPush(redisKey, adInfo);
        }

        redisTemplate.opsForValue().set("banner:lastUpdateTime", LocalDate.now().toString());
    }

    private static List<AdvertisementStatus> getActiveStatusList() {
        return List.of(AdvertisementStatus.PUBLISHED,
                AdvertisementStatus.PENDING_CANCEL);
    }
}
