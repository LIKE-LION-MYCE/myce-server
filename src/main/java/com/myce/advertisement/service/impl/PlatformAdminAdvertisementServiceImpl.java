package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.DetailApplyAdvertisement;
import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdvertisementRepository;
import com.myce.advertisement.service.PlatformAdminAdvertisementService;
import com.myce.advertisement.service.mapper.AdvertisementMapper;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformAdminAdvertisementServiceImpl implements PlatformAdminAdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;
    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    public PageResponse<SimpleApplyAdvertisement> getAllApplyList(int page, int pageSize, boolean latestFirst) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        List<AdvertisementStatus> applyStatus = List.of(
                AdvertisementStatus.PENDING_APPROVAL,
                AdvertisementStatus.PENDING_PAYMENT,
                AdvertisementStatus.REJECTED,
                AdvertisementStatus.COMPLETED);

        Page<Advertisement> bannerEntityPage = advertisementRepository.findByStatusIn(applyStatus, pageable);

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    public PageResponse<SimpleApplyAdvertisement> getFilteredApplyListByKeyword(String keyword, String statusText,
            int page, int pageSize, boolean latestFirst) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        AdvertisementStatus status;
        Page<Advertisement> bannerEntityPage;

        if (AdvertisementStatus.fromString(statusText) != null) {
            status = AdvertisementStatus.valueOf(statusText);
            bannerEntityPage = advertisementRepository.findByTitleContainingAndStatus(keyword, status, pageable);
        } else {
            bannerEntityPage = advertisementRepository.findByTitleContaining(keyword, pageable);
        }

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    public DetailApplyAdvertisement getDetail(Long bannerId) {
        Advertisement advertisement = advertisementRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BANNER_NOT_EXIST));
        return getDetailApplyAdvertisement(advertisement);
    }


    // DTO 변환
    private SimpleApplyAdvertisement getSimpleApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return AdvertisementMapper.getSimpleAdvertisement(advertisement, businessProfile);
    }

    private DetailApplyAdvertisement getDetailApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return AdvertisementMapper.getDetailAdvertisement(advertisement, businessProfile);
    }
}
