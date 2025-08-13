package com.myce.system.service.adposition;

import com.myce.common.dto.PageResponse;
import com.myce.system.dto.adposition.AdPositionDropdownResponse;
import com.myce.system.dto.adposition.AdPositionResponse;

import java.util.List;

public interface AdPositionService {
  List<AdPositionDropdownResponse> getAdPositionDropdown();

  PageResponse<AdPositionResponse> getAdPositionList(int page, int pageSize);
}
