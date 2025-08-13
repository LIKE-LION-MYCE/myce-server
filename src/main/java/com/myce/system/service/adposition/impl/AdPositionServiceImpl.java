package com.myce.system.service.adposition.impl;

import com.myce.common.dto.PageResponse;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.adposition.AdPositionDetailResponse;
import com.myce.system.dto.adposition.AdPositionDropdownResponse;
import com.myce.system.entity.AdPosition;
import com.myce.system.repository.AdPositionRepository;
import com.myce.system.dto.adposition.AdPositionResponse;
import com.myce.system.service.adposition.AdPositionService;
import com.myce.system.service.mapper.AdPositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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
}
