package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.AdPositionResponse;
import com.myce.advertisement.repository.AdPositionRepository;
import com.myce.advertisement.service.AdPositionService;
import com.myce.advertisement.service.mapper.AdPositionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdPositionServiceImpl implements AdPositionService {
  private final AdPositionRepository adPositionRepository;

  @Override
  public List<AdPositionResponse> getAdPositions() {
    return adPositionRepository.findAll()
        .stream()
        .map(AdPositionMapper::toDto)
        .toList();
  }
}
