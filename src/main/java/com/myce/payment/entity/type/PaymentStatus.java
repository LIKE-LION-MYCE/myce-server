package com.myce.payment.entity.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제 대기"),
    SUCCESS("결제 성공"),
    FAILED("결제 실패"),
    REFUNDED("환불"),
    PARTIAL_REFUNDED("부분 환불");

    private final String label;

    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) return status;
        }
        throw new CustomException(CustomErrorCode.PAYMENT_STATUS_INVALID);
    }

    public static PaymentStatus fromLabel(String label) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getLabel().equals(label)) return status;
        }
        throw new CustomException(CustomErrorCode.PAYMENT_STATUS_INVALID);
    }
}
