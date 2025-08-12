package com.myce.system.service.fee;

import com.myce.system.dto.fee.ExpoFeeListResponse;
import com.myce.system.dto.fee.ExpoFeeRequest;

public interface ExpoFeeService {
    void saveExpoFee(ExpoFeeRequest request);

    ExpoFeeListResponse getExpoFeeList(int page, String name);
}
