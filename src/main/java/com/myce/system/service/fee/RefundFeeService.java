package com.myce.system.service.fee;

import com.myce.system.dto.fee.PublicRefundPolicyListResponse;
import com.myce.system.dto.fee.RefundFeeListResponse;

public interface RefundFeeService {

    RefundFeeListResponse getAllSettings(int page);
    
    PublicRefundPolicyListResponse getActivePublicRefundPolicy();
}