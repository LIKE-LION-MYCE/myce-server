package com.myce.advertisement.service.impl;

import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdPositionRepository;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.advertisement.service.ManageAdvertisementService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageAdvertisementServiceImpl implements ManageAdvertisementService {
    private final AdvertisementRepository adRepository;
    private final AdPositionRepository adPositionRepository;

    public void checkAvailablePeriod(Long locationId,
            LocalDate startedAt, LocalDate endedAt) {
        AdPosition requestedAdPosition = adPositionRepository.findById(locationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BANNER_POSITION_NOT_EXIST));
        List<AdvertisementStatus> activeStatusList = List.of(AdvertisementStatus.PUBLISHED,
            AdvertisementStatus.PENDING_CANCEL);

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
}
