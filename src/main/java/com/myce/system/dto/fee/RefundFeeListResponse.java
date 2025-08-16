package com.myce.system.dto.fee;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class RefundFeeListResponse {
    private final int currentPage;
    private final int totalPage;
    private final List<RefundFeeResponse> refundFees;

    public RefundFeeListResponse(int currentPage, int totalPage) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.refundFees = new ArrayList<>();
    }

    public void addRefundFee(RefundFeeResponse refundFee) {
        this.refundFees.add(refundFee);
    }
}