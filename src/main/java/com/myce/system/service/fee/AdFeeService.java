package com.myce.system.service.fee;

import com.myce.system.dto.fee.AdFeeListResponse;
import com.myce.system.dto.fee.AdFeeRequest;
import com.myce.system.dto.fee.FeeActiveRequest;

public interface AdFeeService {

    void saveAdFee(AdFeeRequest request);

    AdFeeListResponse getAdFeeList(int page, Long positionId, String name);

    void updateAdFeeActivation(Long targetId, FeeActiveRequest request);
}
