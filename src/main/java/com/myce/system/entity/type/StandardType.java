package com.myce.system.entity.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StandardType {
    AFTER_RESERVATION("예매 후"),
    BEFORE_EXPO_START("관람 전");

    private final String description;

    public static StandardType fromString(String value) {
        for (StandardType type : StandardType.values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }

        throw new CustomException(CustomErrorCode.NOT_EXIST_REFUND_TYPE);
    }
}
