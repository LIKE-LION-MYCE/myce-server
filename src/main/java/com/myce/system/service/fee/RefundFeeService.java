package com.myce.system.service.fee;

import com.myce.system.dto.fee.PublicRefundPolicyListResponse;
import com.myce.system.dto.fee.FeeActiveRequest;
import com.myce.system.dto.fee.RefundFeeListResponse;
import com.myce.system.dto.fee.RefundFeeRequest;
import com.myce.system.dto.fee.UpdateRefundFeeRequest;

public interface RefundFeeService {

    void saveRefundFee(RefundFeeRequest request);

    RefundFeeListResponse getAllSettings(int page, String name);

    void updateRefundFee(long id, UpdateRefundFeeRequest request);

    void updateRefundFeeActivation(long id, FeeActiveRequest request);

    PublicRefundPolicyListResponse getActivePublicRefundPolicy();
}