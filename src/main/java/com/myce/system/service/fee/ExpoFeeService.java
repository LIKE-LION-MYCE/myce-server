package com.myce.system.service.fee;

import com.myce.system.dto.fee.ExpoFeeListResponse;
import com.myce.system.dto.fee.ExpoFeeRequest;
import com.myce.system.dto.fee.ExpoFeeResponse;
import com.myce.system.dto.fee.FeeActiveRequest;

public interface ExpoFeeService {
    void saveExpoFee(ExpoFeeRequest request);

    ExpoFeeListResponse getExpoFeeList(int page, String name);

    void updateExpoFeeActivation(Long targetId, FeeActiveRequest request);

    ExpoFeeResponse getActiveExpoFee();
}
