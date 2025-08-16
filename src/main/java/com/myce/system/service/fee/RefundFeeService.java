package com.myce.system.service.fee;

import com.myce.system.dto.fee.RefundFeeListResponse;

public interface RefundFeeService {

    RefundFeeListResponse getAllSettings(int page);
}