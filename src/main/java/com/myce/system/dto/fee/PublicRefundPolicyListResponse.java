package com.myce.system.dto.fee;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PublicRefundPolicyListResponse {
    private final List<PublicRefundPolicyResponse> policies;
    
    public PublicRefundPolicyListResponse() {
        this.policies = new ArrayList<>();
    }
    
    public void addPolicy(PublicRefundPolicyResponse policy) {
        this.policies.add(policy);
    }
    
    public static PublicRefundPolicyListResponse from(RefundFeeListResponse refundFeeList) {
        PublicRefundPolicyListResponse response = new PublicRefundPolicyListResponse();
        
        for (RefundFeeResponse refundFee : refundFeeList.getRefundFees()) {
            PublicRefundPolicyResponse policy = PublicRefundPolicyResponse.from(refundFee);
            response.addPolicy(policy);
        }
        
        return response;
    }
}