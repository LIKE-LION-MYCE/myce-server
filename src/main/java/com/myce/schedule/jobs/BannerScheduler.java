package com.myce.schedule.jobs;

import com.myce.advertisement.dto.MainPageAdInfo;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.advertisement.service.ManageAdvertisementService;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BannerScheduler implements TaskScheduler {
    private final ManageAdvertisementService manageAdvertisementService;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        log.debug("[Scheduler] Registered banner scheduler.");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        try {
            log.debug("[Scheduler] ApplicationReadyEvent - initial banner processing start.");
            this.process();
            log.debug("[Scheduler] ApplicationReadyEvent - initial banner processing done.");
        } catch (Exception e) {
            log.error("Fail to run initial banner processing on ApplicationReadyEvent.", e);
        }
    }

    @Override
    @Scheduled(cron = "${scheduler.days}")
    public void run() {
        try{
            this.process();
        }catch (Exception e){
            log.error("Fail to run banner scheduler.", e);
        }
    }

    @Override
    public void process() {
        int published = manageAdvertisementService.publishPendingAds();
        int completed = manageAdvertisementService.closeCompletedAds();

        boolean needDateRefresh = shouldRefreshByDate();
        if (needDateRefresh || published > 0 || completed > 0) {
            manageAdvertisementService.refreshBannerCache();
        }
    }

    private boolean shouldRefreshByDate() {
        LocalDate today = LocalDate.now();
        String lastUpdateTimeString = (String) redisTemplate.opsForValue().get("banner:lastUpdateTime");
        return lastUpdateTimeString == null
                || today.isAfter(LocalDate.parse(lastUpdateTimeString, DateTimeFormatter.ISO_DATE));
    }
}
