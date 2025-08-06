package com.myce.advertisement.service;

import com.myce.advertisement.dto.AdPositionResponse;
import java.util.List;

public interface AdPositionService {
  List<AdPositionResponse> getAdPositions();
}
