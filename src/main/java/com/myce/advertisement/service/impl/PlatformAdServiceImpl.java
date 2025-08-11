package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.AdSimpleResponse;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.advertisement.repository.AdRepository;
import com.myce.advertisement.service.PlatformAdService;
import com.myce.advertisement.service.mapper.AdMapper;
import com.myce.common.dto.PageResponse;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformAdServiceImpl implements PlatformAdService {

    private final AdRepository adRepository;
    private final BusinessProfileRepository businessProfileRepository;

    public PageResponse<AdSimpleResponse> getAllAdList(
            int page, int pageSize,
            boolean latestFirst, boolean isApply) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        List<AdvertisementStatus> applyStatusList = getApplyStatusList(isApply);

        Page<Advertisement> bannerEntityPage = adRepository
                .findByStatusIn(applyStatusList, pageable);

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    public PageResponse<AdSimpleResponse> getFilteredAdListByKeyword(
            String keyword, String statusText,
            int page, int pageSize, boolean latestFirst, boolean isApply) {
        Sort sort = latestFirst ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Advertisement> bannerEntityPage;

        List<AdvertisementStatus> applyStatusList = getApplyStatusList(isApply);
        AdvertisementStatus requestedStatus = AdvertisementStatus.fromString(statusText);

        if (requestedStatus != null && applyStatusList.contains(requestedStatus)) {
            bannerEntityPage = adRepository
                    .findByTitleContainingAndStatus(keyword, requestedStatus, pageable);
        } else {
            bannerEntityPage = adRepository
                    .findByTitleContainingAndStatusIn(keyword, applyStatusList, pageable);
        }

        return PageResponse.from(bannerEntityPage.map(this::getSimpleApplyAdvertisement));
    }

    private List<AdvertisementStatus> getApplyStatusList(boolean isApply) {
        if (isApply) {
            return List.of(AdvertisementStatus.PENDING_APPROVAL,
                    AdvertisementStatus.PENDING_PAYMENT,
                    AdvertisementStatus.PENDING_PUBLISH,
                    AdvertisementStatus.REJECTED,
                    AdvertisementStatus.CANCELLED,
                    AdvertisementStatus.COMPLETED);
        } else {
            return List.of(AdvertisementStatus.PUBLISHED,
                    AdvertisementStatus.PENDING_CANCEL);
        }
    }

    // DTO 변환
    private AdSimpleResponse getSimpleApplyAdvertisement(Advertisement advertisement) {
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(advertisement.getId(), TargetType.ADVERTISEMENT)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BUSINESS_NOT_EXIST));

        return AdMapper.getSimpleAdvertisement(advertisement, businessProfile);
    }
}
