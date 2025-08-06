package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.SimpleApplyAdvertisement;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.service.mapper.AdvertisementMapper;
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
        List<AdvertisementStatus> applyStatusList = getApplyStatusList();

        Page<Advertisement> bannerEntityPage = advertisementRepository.findByStatusIn(applyStatusList, pageable);

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    public PageResponse<SimpleApplyAdvertisement> getFilteredApplyListByKeyword(String keyword, String statusText,
            int page, int pageSize, boolean latestFirst) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Advertisement> bannerEntityPage;

        List<AdvertisementStatus> applyStatusList = getApplyStatusList();
        AdvertisementStatus requestedStatus = AdvertisementStatus.fromString(statusText);

        if (requestedStatus != null && applyStatusList.contains(requestedStatus)) {
            bannerEntityPage = advertisementRepository.findByTitleContainingAndStatus(keyword, requestedStatus, pageable);
        } else {
            bannerEntityPage = advertisementRepository.findByTitleContainingAndStatusIn(keyword, applyStatusList, pageable);
        }

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }



    private List<AdvertisementStatus> getApplyStatusList() {
        return List.of(AdvertisementStatus.PENDING_APPROVAL,
                        AdvertisementStatus.PENDING_PAYMENT,
                        AdvertisementStatus.REJECTED,
                        AdvertisementStatus.COMPLETED);
    }
    // DTO 변환
    private SimpleApplyAdvertisement getSimpleApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return AdvertisementMapper.getSimpleAdvertisement(advertisement, businessProfile);
    }
}
