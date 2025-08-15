package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.AdDetailResponse;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdRepository;
import com.myce.advertisement.service.PlatformAdDetailService;
import com.myce.advertisement.service.mapper.AdMapper;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformAdDetailServiceImpl implements PlatformAdDetailService {
    private final AdRepository adRepository;
    private final BusinessProfileRepository businessProfileRepository;

    public AdDetailResponse getDetail(Long adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
        log.info("Detail Advertisement : {}", ad);
        return getDetailApplyAdvertisement(ad);
    }

    private AdDetailResponse getDetailApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));
        log.info("Detail BusinessProfile : {}", businessProfile);
        return AdMapper.getDetailAdvertisement(advertisement, businessProfile);
    }
}
