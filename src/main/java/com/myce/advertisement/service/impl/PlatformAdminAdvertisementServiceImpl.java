package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.mapper.AdvertisementMapper;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.advertisement.service.PlatformAdminAdvertisementService;
import com.myce.common.dto.PageResponse;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

@Service
public class PlatformAdminAdvertisementServiceImpl implements PlatformAdminAdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;
    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    List<AdvertisementStatus> applyStatus = List.of(
            AdvertisementStatus.PENDING_APPROVAL,
            AdvertisementStatus.PENDING_PAYMENT,
            AdvertisementStatus.REJECTED,
            AdvertisementStatus.COMPLETED);

    public PageResponse<SimpleApplyAdvertisement> getList(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Advertisement> bannerEntityPage = advertisementRepository.findByStatusIn(applyStatus, pageable);

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    public PageResponse<SimpleApplyAdvertisement> filterList(String keyword, String statusText, int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize);
        AdvertisementStatus status = AdvertisementStatus.valueOf(statusText);

        Page<Advertisement> bannerEntityPage = advertisementRepository.findByTitleContainingAndStatus(keyword, status, pageable);

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }



    // DTO 변환
    private SimpleApplyAdvertisement getSimpleApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return AdvertisementMapper.getApplyBanner(advertisement, businessProfile);
    }
}
