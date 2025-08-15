package com.myce.system.service.adposition.impl;

import com.myce.common.dto.PageResponse;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.adposition.*;
import com.myce.system.entity.AdPosition;
import com.myce.system.repository.AdPositionRepository;
import com.myce.system.service.adposition.AdPositionService;
import com.myce.system.service.mapper.AdPositionMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdPositionServiceImpl implements AdPositionService {
    private final AdPositionRepository adPositionRepository;

    @Override
    public List<AdPositionDropdownResponse> getAdPositionDropdown() {
        return adPositionRepository.findAll()
                .stream()
                .map(AdPositionMapper::toDto)
                .toList();
    }

    @Override
    public PageResponse<AdPositionResponse> getAdPositionList(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<AdPosition> adPositions = adPositionRepository.findAll(pageable);

        return PageResponse.from(AdPositionMapper.toListDto(adPositions));
    }

    @Override
    public AdPositionDetailResponse getAdPositionDetail(long positionId) {
        AdPosition adPosition = adPositionRepository.findById(positionId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_POSITION_NOT_EXIST));

        return AdPositionMapper.toDetailDto(adPosition);
    }

    @Override
    @Transactional
    public void updateAdPosition(long bannerId, AdPositionUpdateRequest request) {
        AdPosition adPosition = adPositionRepository.findById(bannerId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.AD_POSITION_NOT_EXIST));

        adPosition.update(request.getBannerName(),
                request.getBannerWidth(),
                request.getBannerHeight(),
                request.getMaxBannerCount(),
                request.isActive(),
                LocalDateTime.now());

        log.info("AdPosition({}) updated", adPosition.getId());
    }

    @Override
    @Transactional
    public void addAdPosition(AdPositionNewRequest request) {
        AdPosition adPosition = AdPositionMapper.toEntity(request);

        adPositionRepository.save(adPosition);
        log.info("AdPosition({}) created", adPosition.getId());
    }

    @Override
    @Transactional
    public void deleteAdPosition(long bannerId) {
        adPositionRepository.deleteById(bannerId);
    }
}
