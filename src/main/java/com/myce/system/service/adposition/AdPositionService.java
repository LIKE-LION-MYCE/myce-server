package com.myce.system.service.adposition;

import com.myce.common.dto.PageResponse;
import com.myce.system.dto.adposition.AdPositionDetailResponse;
import com.myce.system.dto.adposition.AdPositionDropdownResponse;
import com.myce.system.dto.adposition.AdPositionResponse;
import com.myce.system.dto.adposition.AdPositionUpdateRequest;

import java.util.List;

public interface AdPositionService {
  List<AdPositionDropdownResponse> getAdPositionDropdown();

  PageResponse<AdPositionResponse> getAdPositionList(int page, int pageSize);

  AdPositionDetailResponse getAdPositionDetail(long positionId);

  void updateAdPosition(long bannerId, AdPositionUpdateRequest request);
}
