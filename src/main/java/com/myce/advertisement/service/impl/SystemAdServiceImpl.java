package com.myce.advertisement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.advertisement.dto.AdMainPageInfo;
import com.myce.system.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.system.repository.AdPositionRepository;
import com.myce.advertisement.repository.AdRepository;
import com.myce.advertisement.service.SystemAdService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemAdServiceImpl implements SystemAdService {
    private final AdRepository adRepository;
    private final AdPositionRepository adPositionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationService notificationService;

    public void checkAvailablePeriod(Long locationId,
                                     LocalDate startedAt, LocalDate endedAt) {
        AdPosition requestedAdPosition = adPositionRepository.findById(locationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_POSITION_NOT_EXIST));
        List<AdvertisementStatus> activeStatusList = AdvertisementStatus.ACTIVE_STATUSES;

        List<Advertisement> activeAds = adRepository.findOverlappingAds(
                startedAt, endedAt, activeStatusList, locationId);

        //기간 별로 최대 조건을 넘는지 탐색
        LocalDate date;
        for (date = startedAt; date.isBefore(endedAt.plusDays(1)); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            long overlappingCount = activeAds.stream()
                    .filter(ad -> !finalDate.isBefore(ad.getDisplayStartDate()) &&
                            !finalDate.isAfter(ad.getDisplayEndDate()) &&
                            !AdvertisementStatus.EXPIRED_STATUSES.contains(ad.getStatus()))
                    .count();
            //해당 날짜의 배너 수가 최댓값일때
            if (overlappingCount >= requestedAdPosition.getMaxCount()) {
                throw new CustomException(CustomErrorCode.AD_MAX_CAPACITY_REACHED);
            }
        }
    }

    // 게시중인 배너 조회
    public List<AdMainPageInfo> getActiveAds() {
        Set<String> keys = redisTemplate.keys("ad:list:*");
        List<Object> totalBanners = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String key : keys) {
            List<Object> banners = redisTemplate.opsForList().range(key, 0, -1);
            if(banners != null && !banners.isEmpty()) {
                totalBanners.addAll(banners);
            }
        }

        return Objects.requireNonNull(totalBanners).stream()
                .map(banner -> objectMapper.convertValue(banner, AdMainPageInfo.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAdStatus() {
        int published = publishPendingAds();
        int completed = closeCompletedAds();

        boolean needDateRefresh = shouldRefreshByDate();
        if (needDateRefresh || published > 0 || completed > 0) {
            refreshAdCache();
        }
    }

    // 스케줄러 작업 수행
    @Transactional
    public int publishPendingAds() {
        LocalDate now = LocalDate.now();
        log.info("@@@@@@@@@@@@@@@@ now date : {}", now.toString());
        log.info("[publishPendingAds] find publishing pending ads. nowDate : {}", now);

        List<Advertisement> pendingAds = adRepository
                .findAllByDisplayStartDateLessThanEqualAndStatus(
                        LocalDate.now(),
                        AdvertisementStatus.PENDING_PUBLISH);

        log.info("@@@@@@ find ad: {}", pendingAds.size());
        for (Advertisement ad : pendingAds) {
            String oldStatus = ad.getStatus().name();
            ad.publish();
            String newStatus = ad.getStatus().name();
            
            // 상태 변경 알림 전송
            try {
                notificationService.sendAdvertisementStatusChangeNotification(ad.getId(), ad.getTitle(), oldStatus, newStatus);
            } catch (Exception e) {
                log.warn("광고 자동 게시 알림 전송 실패 - adId: {}, 오류: {}", ad.getId(), e.getMessage());
            }
        }
        if (!pendingAds.isEmpty()) {
            adRepository.saveAll(pendingAds);
        }
        return pendingAds.size();
    }

    // 게시 종료: 종료일 < 오늘, 상태가 PUBLISHED
    @Transactional
    public int closeCompletedAds() {
        List<Advertisement> endedAds = adRepository
                .findAllByDisplayEndDateLessThanAndStatus(
                        LocalDate.now(),
                        AdvertisementStatus.PUBLISHED);

        for (Advertisement ad : endedAds) {
            String oldStatus = ad.getStatus().name();
            ad.complete();
            String newStatus = ad.getStatus().name();
            
            // 상태 변경 알림 전송
            try {
                notificationService.sendAdvertisementStatusChangeNotification(ad.getId(), ad.getTitle(), oldStatus, newStatus);
            } catch (Exception e) {
                log.warn("광고 게시 종료 알림 전송 실패 - adId: {}, 오류: {}", ad.getId(), e.getMessage());
            }
        }
        if (!endedAds.isEmpty()) {
            adRepository.saveAll(endedAds);
        }
        return endedAds.size();
    }

    // 게시중인 배너 수집(업데이트 날짜가 오늘이 아니면)
    public void refreshAdCache() {
        List<AdvertisementStatus> activeStatusList = AdvertisementStatus.ACTIVE_STATUSES;
        List<Advertisement> allPublishedAds = adRepository
                .findAdsActiveTodayAndStatusIn(activeStatusList);

        Set<String> keys = redisTemplate.keys("ad:list:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        for (Advertisement ad : allPublishedAds) {
            AdMainPageInfo adInfo = new AdMainPageInfo(
                    ad.getId(),
                    ad.getAdPosition().getId(),
                    ad.getImageUrl(),
                    ad.getLinkUrl());

            String redisKey = "ad:list:" + adInfo.getLocationId();
            redisTemplate.opsForList().rightPush(redisKey, adInfo);
        }

        redisTemplate.opsForValue().set("ad:lastUpdateTime", LocalDate.now().toString());
    }

    private boolean shouldRefreshByDate() {
        LocalDate today = LocalDate.now();
        String lastUpdateTimeString = (String) redisTemplate.opsForValue().get("ad:lastUpdateTime");
        return lastUpdateTimeString == null
                || today.isAfter(LocalDate.parse(lastUpdateTimeString, DateTimeFormatter.ISO_DATE));
    }
}
