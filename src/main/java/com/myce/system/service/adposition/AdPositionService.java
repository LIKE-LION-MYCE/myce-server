package com.myce.system.service.adposition;

import com.myce.common.dto.PageResponse;
import com.myce.system.dto.adposition.*;

import java.util.List;

public interface AdPositionService {
  List<AdPositionDropdownResponse> getAdPositionDropdown();

  PageResponse<AdPositionResponse> getAdPositionList(int page, int pageSize);

  AdPositionDetailResponse getAdPositionDetail(long positionId);

  void updateAdPosition(long bannerId, AdPositionUpdateRequest request);

  void addAdPosition(AdPositionNewRequest request);

  void deleteAdPosition(long bannerId);
}
